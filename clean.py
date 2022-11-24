import os

removedFiles = 0

for file in os.listdir("bin/dylanbruner"):
    if file.endswith(".class"): os.remove(os.path.join("bin/dylanbruner", file)); removedFiles += 1

print("Cleaned " + str(removedFiles) + " files")