"""Publish the jars that nexus.gtnewhorizons.com refuses to serve into a local
Maven repository, so Gradle can build without the network path that is broken.

Why this exists
---------------
On this machine `nexus.gtnewhorizons.com` answers `.pom`/`.module`/`.sha1`
requests normally but never delivers the body of a `.jar` (connection times out
after the headers). GitHub Releases, Maven Central and services.gradle.org are
all fine. So:

  * jars are pulled from GitHub Releases (GTNH publishes every artifact there),
  * poms are pulled from the Nexus (text works) and written next to the jar,
  * `.gradle-home/init.d/local-mirror.gradle` puts this repository in front of
    the Nexus, so Gradle finds the jar locally and still resolves metadata.

Usage
-----
    python tools/env/mirror.py            # fetch everything missing
    python tools/env/mirror.py --list     # show the manifest and file status
    python tools/env/mirror.py --check    # same, but exit 1 if something is missing
"""

import os
import ssl
import sys
import time
import urllib.error
import urllib.request
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
REPO = os.path.join(ROOT, ".tools", "local-maven")
DOWNLOADS = os.path.join(ROOT, ".tools", "downloads")

NEXUS = "https://nexus.gtnewhorizons.com/repository/public/"
GH = "https://github.com/"

CTX = ssl.create_default_context()


def gh_release(repo, tag, asset):
    return f"{GH}{repo}/releases/download/{tag}/{asset}"


# (group, artifact, version, classifier, extension) -> url
MANIFEST = {
    # --- Gradle plugins -----------------------------------------------------
    ("com.gtnewhorizons", "gtnhgradle", "2.0.33", None, "jar"):
        gh_release("GTNewHorizons/GTNHGradle", "2.0.33", "GTNHGradle-2.0.33.jar"),
    ("com.gtnewhorizons", "retrofuturagradle", "2.0.5", None, "jar"):
        gh_release("GTNewHorizons/RetroFuturaGradle", "2.0.5", "retrofuturagradle-2.0.5.jar"),
    ("com.gtnewhorizons", "rfgJavacPlugin", "2.0.5", None, "jar"):
        gh_release("GTNewHorizons/RetroFuturaGradle", "2.0.5", "rfgJavacPlugin-2.0.5.jar"),

    # --- GTNH runtime / compile deps ---------------------------------------
    ("com.github.GTNewHorizons", "GTNHLib", "0.11.48", None, "jar"):
        gh_release("GTNewHorizons/GTNHLib", "0.11.48", "gtnhlib-0.11.48.jar"),
    ("com.github.GTNewHorizons", "GTNHLib", "0.11.48", "dev", "jar"):
        gh_release("GTNewHorizons/GTNHLib", "0.11.48", "gtnhlib-0.11.48-dev.jar"),
    ("com.github.GTNewHorizons", "GTNHLib", "0.11.48", "api", "jar"):
        gh_release("GTNewHorizons/GTNHLib", "0.11.48", "gtnhlib-0.11.48-api.jar"),
    ("com.github.GTNewHorizons", "GTNHLib", "0.11.48", "dev-preshadow", "jar"):
        gh_release("GTNewHorizons/GTNHLib", "0.11.48", "gtnhlib-0.11.48-dev-preshadow.jar"),
    ("com.github.GTNewHorizons", "GTNHLib", "0.11.37", None, "jar"):
        gh_release("GTNewHorizons/GTNHLib", "0.11.37", "gtnhlib-0.11.37.jar"),
    ("com.github.GTNewHorizons", "GTNHLib", "0.11.37", "dev", "jar"):
        gh_release("GTNewHorizons/GTNHLib", "0.11.37", "gtnhlib-0.11.37-dev.jar"),

    ("com.github.GTNewHorizons", "Hodgepodge", "2.7.206", None, "jar"):
        gh_release("GTNewHorizons/Hodgepodge", "2.7.206", "hodgepodge-2.7.206.jar"),
    ("com.github.GTNewHorizons", "Hodgepodge", "2.7.206", "dev", "jar"):
        gh_release("GTNewHorizons/Hodgepodge", "2.7.206", "hodgepodge-2.7.206-dev.jar"),
    ("com.github.GTNewHorizons", "Hodgepodge", "2.7.206", "dev-preshadow", "jar"):
        gh_release("GTNewHorizons/Hodgepodge", "2.7.206", "hodgepodge-2.7.206-dev-preshadow.jar"),

    ("com.github.GTNewHorizons", "lwjgl3ify", "3.0.33", None, "jar"):
        gh_release("GTNewHorizons/lwjgl3ify", "3.0.33", "lwjgl3ify-3.0.33.jar"),
    ("com.github.GTNewHorizons", "lwjgl3ify", "3.0.33", "dev", "jar"):
        gh_release("GTNewHorizons/lwjgl3ify", "3.0.33", "lwjgl3ify-3.0.33-dev.jar"),
    ("com.github.GTNewHorizons", "lwjgl3ify", "3.0.33", "api", "jar"):
        gh_release("GTNewHorizons/lwjgl3ify", "3.0.33", "lwjgl3ify-3.0.33-api.jar"),
    ("com.github.GTNewHorizons", "lwjgl3ify", "3.0.33", "dev-preshadow", "jar"):
        gh_release("GTNewHorizons/lwjgl3ify", "3.0.33", "lwjgl3ify-3.0.33-dev-preshadow.jar"),

    ("com.github.GTNewHorizons", "jabel-stubs", "1.0.0", None, "jar"):
        gh_release("GTNewHorizons/jabel-stubs", "1.0.0", "jabel-stubs-1.0.0.jar"),

    # The one artifact with no GitHub release asset: only the Nexus has it, and
    # it is small enough (27 KB) to be pulled in 16 KB ranges -- see fetch_chunked.
    ("com.github.GTNewHorizons", "jabel-javac-plugin", "1.0.2-GTNH", None, "jar"):
        NEXUS + "com/github/GTNewHorizons/jabel-javac-plugin/1.0.2-GTNH/jabel-javac-plugin-1.0.2-GTNH.jar",
}

# The Nexus closes the connection once a body grows past a few KB (16 KB ranges
# still work), so anything taken from there has to be assembled chunk by chunk.
CHUNK = 16384


def log(msg):
    print(msg, flush=True)


def _sort_key(key):
    """Sort manifest keys; the classifier may be None."""
    return tuple("" if part is None else str(part) for part in key)


def coords_path(group, artifact, version, classifier, ext):
    d = os.path.join(REPO, *group.split("."), artifact, version)
    name = f"{artifact}-{version}" + (f"-{classifier}" if classifier else "") + f".{ext}"
    return d, os.path.join(d, name)


def pom_path(group, artifact, version):
    d = os.path.join(REPO, *group.split("."), artifact, version)
    return os.path.join(d, f"{artifact}-{version}.pom")


def fetch_chunked(url, dest):
    """Download url in small Range requests (the Nexus drops big bodies)."""
    tmp = dest + ".part"
    t0 = time.time()
    total = None
    with open(tmp, "wb") as f:
        offset = 0
        while total is None or offset < total:
            headers = {"User-Agent": "berlit-mirror",
                       "Range": f"bytes={offset}-{offset + CHUNK - 1}"}
            req = urllib.request.Request(url, headers=headers)
            try:
                with urllib.request.urlopen(req, timeout=60, context=CTX) as r:
                    data = r.read()
                    if total is None:
                        cr = r.headers.get("Content-Range") or ""
                        if "/" in cr:
                            total = int(cr.rsplit("/", 1)[1])
            except Exception as e:
                if os.path.exists(tmp):
                    os.remove(tmp)
                return False, f"chunked {type(e).__name__}: {str(e)[:60]} at {offset}"
            if not data:
                break
            f.write(data)
            offset += len(data)
    size = os.path.getsize(tmp)
    if total is not None and size != total:
        os.remove(tmp)
        return False, f"chunked short read {size}/{total}"
    if not zipfile.is_zipfile(tmp):
        os.remove(tmp)
        return False, f"chunked, not a zip ({size} B)"
    os.replace(tmp, dest)
    return True, f"{size} B in {time.time() - t0:.1f}s (chunked)"


def fetch(url, dest, kind):
    """Download url to dest. Returns (ok, detail)."""
    tmp = dest + ".part"
    t0 = time.time()
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "berlit-mirror"})
        with urllib.request.urlopen(req, timeout=120, context=CTX) as r, open(tmp, "wb") as f:
            while True:
                b = r.read(1 << 16)
                if not b:
                    break
                f.write(b)
    except Exception as e:
        if os.path.exists(tmp):
            os.remove(tmp)
        # The Nexus truncates anything past ~16 KB: retry there in ranges.
        if kind == "jar" and "nexus.gtnewhorizons.com" in url:
            return fetch_chunked(url, dest)
        return False, f"{type(e).__name__}: {str(e)[:80]}"
    size = os.path.getsize(tmp)
    if kind == "jar" and not zipfile.is_zipfile(tmp):
        os.remove(tmp)
        return False, f"not a zip ({size} B)"
    os.replace(tmp, dest)
    return True, f"{size} B in {time.time() - t0:.1f}s"


def install(group, artifact, version, classifier, ext, url, quiet=False):
    d, dest = coords_path(group, artifact, version, classifier, ext)
    if os.path.exists(dest) and (ext != "jar" or zipfile.is_zipfile(dest)):
        if not quiet:
            log(f"  ok   {os.path.relpath(dest, REPO)}")
        return True
    os.makedirs(d, exist_ok=True)
    ok, detail = fetch(url, dest, ext)
    log(f"  {'ok  ' if ok else 'FAIL'} {os.path.relpath(dest, REPO)}  ({detail})")
    # Metadata: take the small text files from the Nexus, they still work.
    pom = pom_path(group, artifact, version)
    if ok and not os.path.exists(pom):
        okp, detp = fetch(f"{NEXUS}{'/'.join(group.split('.'))}/{artifact}/{version}/{artifact}-{version}.pom", pom, "pom")
        log(f"       pom {'ok' if okp else 'missing'} {detp}")
    return ok


def install_pom_only(group, artifact, version):
    """A pom without a jar entry: needed for parents/BOMs of the plugins."""
    pom = pom_path(group, artifact, version)
    if os.path.exists(pom):
        return True
    os.makedirs(os.path.dirname(pom), exist_ok=True)
    url = f"{NEXUS}{'/'.join(group.split('.'))}/{artifact}/{version}/{artifact}-{version}.pom"
    ok, detail = fetch(url, pom, "pom")
    log(f"  {'ok  ' if ok else 'FAIL'} {os.path.relpath(pom, REPO)}  ({detail})")
    return ok


def main():
    args = set(sys.argv[1:])
    if "--list" in args or "--check" in args:
        missing = 0
        for (g, a, v, c, e) in sorted(MANIFEST, key=_sort_key):
            _, dest = coords_path(g, a, v, c, e)
            present = os.path.exists(dest)
            if not present:
                missing += 1
            log(f"{'present' if present else 'MISSING'}  {g}:{a}:{v}" + (f":{c}" if c else ""))
        log(f"\n{len(MANIFEST) - missing}/{len(MANIFEST)} present")
        return 1 if (missing and "--check" in args) else 0

    os.makedirs(REPO, exist_ok=True)
    failed = []
    for (g, a, v, c, e), url in ((k, MANIFEST[k]) for k in sorted(MANIFEST, key=_sort_key)):
        if not install(g, a, v, c, e, url):
            failed.append(f"{g}:{a}:{v}" + (f":{c}" if c else ""))
    if failed:
        log(f"\nFAILED: {failed}")
        return 1
    log(f"\nMirror ready: {REPO}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
