import os, shutil

if os.path.exists('bin/dylanbruner'): shutil.rmtree('bin/dylanbruner')
os.mkdir('bin/dylanbruner')
if os.path.exists('C:/robocode/robots/dylanbruner'): shutil.rmtree('C:/robocode/robots/dylanbruner')
os.mkdir('C:/robocode/robots/dylanbruner')
print('[INFO] Cleaned bin/dylanbruner')