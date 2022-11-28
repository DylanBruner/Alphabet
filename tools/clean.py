import os, shutil

if os.path.exists('bin/dylanbruner'): shutil.rmtree('bin/dylanbruner')
os.mkdir('bin/dylanbruner')
print('[INFO] Cleaned bin/dylanbruner')