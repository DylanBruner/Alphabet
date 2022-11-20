import os, shutil

BASE_COMMAND   = 'javac -classpath libs/*; -d bin -sourcepath dylanb dylanb/Alphabet.java dylanb/*.java'
ROBO_CLASS_DIR = 'C:\\robocode\\robots\\dylanb'

print('[INFO] Compiling...')
os.system(BASE_COMMAND)
print('[INFO] Moving output to correct locations...')

for file in os.listdir('bin/dylanb'):
    if not file.endswith('.class'): continue
    if os.path.exists(os.path.join(ROBO_CLASS_DIR, file)):
        os.remove(os.path.join(ROBO_CLASS_DIR, file))
    shutil.copyfile('bin/dylanb/' + file, ROBO_CLASS_DIR + '/' + file)