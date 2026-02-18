import sqlite3, re, json 
dq = chr(34) 
attr_pat = ':' + dq + '([^' + dq + ']*)' + dq + ':Attribute' 
op_pat = ':' + dq + '([^' + dq + ']*)' + dq + ':Operation' 
class_pat = ':' + dq + '([^' + dq + ']*)' + dq + ':Class' 
conn=sqlite3.connect('VPP/Ingegneria_del_software_25_26.vpp') 
cur=conn.cursor() 
cur.execute('SELECT NAME, DEFINITION FROM MODEL_ELEMENT WHERE MODEL_TYPE=?', ('Class',)) 
classes = {} 
for name, defn in cur.fetchall(): 
    text = defn.decode('utf-8', 'ignore') if defn else '' 
    if not name: 
        m = re.search(class_pat, text) 
        if m: 
            name = m.group(1) 
    if not name: 
        continue 
    attrs = re.findall(attr_pat, text) 
    ops = re.findall(op_pat, text) 
    classes[name] = {'attributes': attrs, 'operations': ops} 
open('tmp_vpp_classes.json','w', encoding='utf-8').write(json.dumps(classes, ensure_ascii=False, indent=2)) 
