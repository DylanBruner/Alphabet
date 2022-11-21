import os

totalCharacters = 0
totalLines      = 0

for file in os.listdir('dylanbruner'):
    if file.endswith('.java'):
        with open('dylanbruner/'+file, 'r') as f:
            data = f.read()
            totalCharacters += len(data.replace(' ',''))
            totalLines      += len(data.split('\n'))
    
print(f"Total characters: {totalCharacters}")
print(f"Total lines:      {totalLines}")