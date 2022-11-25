import os, shutil, hashlib

BASE_COMMAND   = 'javac -classpath libs/*; -d bin -sourcepath dylanbruner dylanbruner/Alphabet.java dylanbruner/*.java'
ROBO_CLASS_DIR = 'C:\\robocode\\robots\\dylanbruner'

def getFileHash(filename):
    if not os.path.exists(filename): return None
    with open(filename, 'rb') as f:
        return hashlib.md5(f.read()).hexdigest()

print('[INFO] Compiling...')
os.system(BASE_COMMAND)
print('[INFO] Moving output to correct locations...')

copiedFiles, skippedFiles = 0, 0
for file in os.listdir('bin/dylanbruner'):
    if not file.endswith('.class'): continue
    
    if getFileHash(os.path.join('bin/dylanbruner', file)) != getFileHash(os.path.join(ROBO_CLASS_DIR, file)):
        shutil.copyfile('bin/dylanbruner/' + file, ROBO_CLASS_DIR + '/' + file)
        copiedFiles += 1
    else:
        skippedFiles += 1
print(f"[INFO] Copied {copiedFiles} files, skipped {skippedFiles} files.")