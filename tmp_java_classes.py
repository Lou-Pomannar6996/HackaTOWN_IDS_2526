import os, re, json 
classes = [] 
for root, dirs, files in os.walk('src/main/java'): 
    for f in files: 
        if f.endswith('.java'): 
            path = os.path.join(root, f) 
            text = open(path, encoding='utf-8').read() 
            m = re.search(r'\b(class|interface|enum)\s+(\w+)', text) 
            if m: 
                classes.append({'path': path, 'kind': m.group(1), 'name': m.group(2)}) 
open('tmp_java_classes.json','w', encoding='utf-8').write(json.dumps(classes, ensure_ascii=False, indent=2)) 
