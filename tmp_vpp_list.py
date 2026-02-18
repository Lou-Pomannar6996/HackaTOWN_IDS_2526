import json 
data=json.load(open('tmp_vpp_classes.json', encoding='utf-8')) 
for name in sorted(data.keys()): 
    print(name) 
