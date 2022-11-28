import os, shutil, hashlib, copy

BASE_COMMAND   = 'tools\compile.bat'
ROBO_CLASS_DIR = 'C:\\robocode\\robots\\dylanbruner'
if not os.path.exists(ROBO_CLASS_DIR): os.mkdir(ROBO_CLASS_DIR)

def getFileHash(filename):
    if not os.path.exists(filename): return None
    with open(filename, 'rb') as f:
        return hashlib.md5(f.read()).hexdigest()

print('[INFO] Compiling...')
if os.system(BASE_COMMAND) != 0:
    print('[ERROR] Compilation failed!'); exit(1)
    
print('[INFO] Moving output to correct locations...')

copiedFiles, skippedFiles = 0, 0

if os.path.exists('bin/dylanbruner'): shutil.rmtree(ROBO_CLASS_DIR)
shutil.copytree('bin/dylanbruner', ROBO_CLASS_DIR)
print(f"[INFO] Copied {copiedFiles} files, skipped {skippedFiles} files.")