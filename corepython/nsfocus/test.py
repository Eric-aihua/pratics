__author__ = 'eric.sun'


partitions=['2016080217','2016080218',
'2016080219',
'2016080220',
'2016080221',
'2016080222',
'2016080223',
'2016080300',
'2016080301',
'2016080302',
'2016080303',
'2016080304',
'2016080305',
'2016080306',
'2016080307',
'2016080308',
'2016080309',
'2016080310',
'2016080311',
'2016080312',
'2016080316',
'2016080318',
'2016080319',
'2016080320',
'2016080321',
'2016080322',
'2016080323',
'2016080400',
'2016080402',
'2016080403',
'2016080404',
'2016080405',
'2016080406',
'2016080407',
'2016080408',
'2016080409',
'2016080410',
'2016080411',
'2016080412',
'2016080413',]

for par in partitions:
    drop_sql="ALTER TABLE host_vul_count DROP PARTITION (log_time='"+par+"');"
    add_sql="ALTER TABLE host_vul_count ADD  PARTITION (log_time='"+par+"') location '/user/hive/warehouse/host_vul_count/"+par+"';"
    print drop_sql
    print add_sql
