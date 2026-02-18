import sqlite3, re 
conn=sqlite3.connect('VPP/Ingegneria_del_software_25_26.vpp') 
cur=conn.cursor() 
cur.execute('SELECT NAME, DEFINITION FROM MODEL_ELEMENT WHERE MODEL_TYPE=?', ('Class',)) 
rows=cur.fetchall() 
with_ops=[name for name,defn in rows if defn and b'Operation' in defn] 
print('classes_with_Operation', len(with_ops)) 
print(with_ops[:20]) 
