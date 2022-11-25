import zipfile, os


print("=====================================[Compiling]=====================================")
os.system('python tools/compile.py')
print("=====================================[Packaging]=====================================")
with open("Alphabet.properties", "r") as f:
    properties = f.read().splitlines()

def getProperty(name):
    for line in properties:
        if line.startswith(name):
            return line.split("=")[1]
VERSION = getProperty("robot.version")
CLASS_NAME = getProperty("robot.classname")

if VERSION is None: print("Version not found in properties file"); exit()
if CLASS_NAME is None: print("Class name not found in properties file"); exit()

OUTPUT_FILE = f"bin/package/{CLASS_NAME}_{VERSION}.jar"
print(f"Output file: {OUTPUT_FILE}")
if os.path.exists(OUTPUT_FILE) and input("File already exists. Overwrite? (y/n): ").lower().strip() != "y": print("Exiting..."); exit()

print("Writing...")
with zipfile.ZipFile(OUTPUT_FILE, "w") as jar:
    try:
        #Write a new file to /META-INF/MANIFEST.MF
        jar.writestr("META-INF/MANIFEST.MF", f"Manifest-Version: 1.0\nrobots: {CLASS_NAME}")
        print(f"[INFO] Wrote manifest")
        #Copy all files from /bin/{CLASS_NAME.split(".")[0]} to {CLASS_NAME.split(".")[0]}
        for file in os.listdir(f"bin/{CLASS_NAME.split('.')[0]}"):
            jar.write(f"bin/{CLASS_NAME.split('.')[0]}/{file}", f"{CLASS_NAME.split('.')[0]}/{file}")
        print("[INFO] Wrote class files")
        #Write the properties file into {CLASS_NAME.split(".")[0]}/Alphabet.properties
        jar.write("Alphabet.properties", f"{CLASS_NAME.split('.')[0]}/Alphabet.properties")
        print("[INFO] Wrote properties file")
    except Exception as e:
        print("[ERROR] Failed to write to jar file: " + str(e)); exit()
    print("Robot packaged successfully!")