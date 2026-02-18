import sqlite3, json 
conn=sqlite3.connect('VPP/Ingegneria_del_software_25_26.vpp') 
cur=conn.cursor() 
cur.execute('SELECT DISTINCT MODEL_TYPE FROM MODEL_ELEMENT') 
print(sorted(r[0] for r in cur.fetchall())) 
