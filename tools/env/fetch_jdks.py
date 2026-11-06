"""Download Adoptium/Temurin JDKs into .tools/jdk-<major>.

Schannel (system TLS) is broken on this machine, so everything goes through
Python's own OpenSSL. The Adoptium API redirects to the matching GitHub release
asset, which is reachable here.

Usage:  python tools/env/fetch_jdks.py [25 21 17 8]
"""

import os
import shutil
import ssl
import sys
import time
import urllib.request
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
TOOLS = os.path.join(ROOT, ".tools")
DOWNLOADS = os.path.join(TOOLS, "downloads")

API = "https://api.adoptium.net/v3/binary/latest/{v}/ga/windows/x64/jdk/hotspot/normal/eclipse"


def log(msg):
    print(msg, flush=True)


def download(url, dest):
    ctx = ssl.create_default_context()
    req = urllib.request.Request(url, headers={"User-Agent": "berlit-fetch"})
    t0 = time.time()
    with urllib.request.urlopen(req, timeout=120, context=ctx) as r, open(dest, "wb") as f:
        total = int(r.headers.get("Content-Length") or 0)
        done = 0
        last = 0.0
        while True:
            chunk = r.read(1 << 20)
            if not chunk:
                break
            f.write(chunk)
            done += len(chunk)
            now = time.time()
            if now - last > 2:
                last = now
                pct = f"{done * 100 // total:3d}%" if total else "  ?%"
                log(f"    {pct} {done / 1e6:7.1f} MB  {done / 1e6 / max(now - t0, .001):5.2f} MB/s")
    return done


def install(major):
    target = os.path.join(TOOLS, f"jdk-{major}")
    if os.path.exists(os.path.join(target, "bin", "java.exe")):
        log(f"JDK {major}: already installed at {target}")
        return True

    zip_path = os.path.join(DOWNLOADS, f"openjdk-{major}.zip")
    url = API.format(v=major)
    log(f"JDK {major}: {url}")
    try:
        size = download(url, zip_path)
    except Exception as e:
        log(f"JDK {major}: DOWNLOAD FAILED {type(e).__name__}: {e}")
        return False
    log(f"JDK {major}: {size / 1e6:.1f} MB downloaded, extracting")

    tmp = os.path.join(DOWNLOADS, f"tmp-{major}")
    shutil.rmtree(tmp, ignore_errors=True)
    os.makedirs(tmp, exist_ok=True)
    try:
        with zipfile.ZipFile(zip_path) as z:
            z.extractall(tmp)
    except Exception as e:
        log(f"JDK {major}: EXTRACT FAILED {type(e).__name__}: {e}")
        return False

    entries = [d for d in os.listdir(tmp) if os.path.isdir(os.path.join(tmp, d))]
    if len(entries) != 1:
        log(f"JDK {major}: unexpected archive layout {entries}")
        return False
    shutil.move(os.path.join(tmp, entries[0]), target)
    shutil.rmtree(tmp, ignore_errors=True)
    os.remove(zip_path)

    ok = os.path.exists(os.path.join(target, "bin", "java.exe"))
    log(f"JDK {major}: {'installed' if ok else 'MISSING bin/java.exe'} -> {target}")
    return ok


def main():
    majors = sys.argv[1:] or ["25", "21", "17", "8"]
    os.makedirs(DOWNLOADS, exist_ok=True)
    failed = [m for m in majors if not install(m)]
    if failed:
        log(f"FAILED: {failed}")
        return 1
    log("All requested JDKs installed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
