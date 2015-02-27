# -*- coding: utf-8 -*-
import hashlib
import web
import time
import os
import urllib2, json
import chardet
import MySQLdb

urls = (
    '/login', 'Login',
    '/register', 'Register',
    '/update_run_info', 'UpdateRunInfo',
    '/inquire_run_information', 'InquireRunInformation',
    '/inquire_rank', 'InquireRank',
    '/new_week', 'NewWeek'
)


class Login:
    def POST(self):
        db = MySQLdb.connect(host='localhost', user='root', passwd='lxb', db='Pedometer', charset='utf8')
        cursor = db.cursor()
        para = web.input()
        username = para['username']
        password = para['password']
        count = cursor.execute("""select * from user where username = %s""", (username,))
        if count == 0:
            cursor.close()
            db.close()
            return 'user does\'t exist'
        else:
            user = cursor.fetchone()
            true_password = user[1]
            cursor.close()
            db.close()
            if password == true_password:
                return 'login successful'
            else:
                return 'wrong password'


class Register:
    def POST(self):
        db = MySQLdb.connect(host='localhost', user='root', passwd='lxb', db='Pedometer', charset='utf8')
        cursor = db.cursor()
        para = web.input()
        username = para['username']
        password = para['password']
        sex = para['sex']
        count = cursor.execute("""select * from user where username = %s""", (username,))
        if count != 0:
            cursor.close()
            db.close()
            return 'username is exist'
        else:
            cursor.execute("""insert into user values(%s,%s,%s,%s,%s,%s,%s,%s)""",
                           (username, password, sex, '175', '65', '10000', '0', '0',))
            db.commit()
            cursor.close()
            db.close()
            return 'register successful'


class UpdateRunInfo:
    def POST(self):
        db = MySQLdb.connect(host='localhost', user='root', passwd='lxb', db='Pedometer', charset='utf8')
        cursor = db.cursor()
        para = web.input()
        username = para['username']
        step = para['step']
        dis = para['dis']
        calories = para['calories']
        cursor.execute("""select now()""")
        date = cursor.fetchone()
        cursor.execute("""insert into run_info values(%s,%s,%s,%s,%s)""",
                       (username, step, dis, calories, date[0],))
        cursor.execute("""select plan, process, distance from user where username = %s""", (username,))
        record = cursor.fetchone()
        plan = float(record[0])
        process = float(record[1])
        dis_sum = float(record[2])
        dis_sum += float(step)
        if plan != 0:
            process = dis_sum / plan
        else:
            process = 1.0
        cursor.execute("""update user set process = %s,distance = %s where username = %s""",
                       (str(process), str(dis_sum), username,))
        db.commit()
        cursor.close()
        db.close()
        return 'update run information successful'


class InquireRunInformation:
    def POST(self):
        db = MySQLdb.connect(host='localhost', user='root', passwd='lxb', db='Pedometer', charset='utf8')
        cursor = db.cursor()
        para = web.input()
        username = para['username']
        cursor.execute("""select year(now())""")
        year = cursor.fetchone()
        cursor.execute("""select month(now())""")
        month = cursor.fetchone()
        cursor.execute("""select day(now())""")
        day = cursor.fetchone()
        result = []
        for i in range(7):
            begin_date = str(year[0]) + '-' + str(month[0]) + '-' + str(int(day[0]) - 6 + i)
            end_date = str(year[0]) + '-' + str(month[0]) + '-' + str(int(day[0]) - 5 + i)
            cursor.execute("""select step from run_info where username = %s and date >= %s and date < %s""",
                           (username, begin_date, end_date,))
            all_records = cursor.fetchall()
            steps = 0
            for record in all_records:
                steps += int(record[0])
            result.append(steps)
        cursor.execute("""select plan from user where username = %s""", (username,))
        plan = cursor.fetchone()
        result.append(int(plan[0]))
        cursor.close()
        db.close()
        return result


class InquireRank:
    def POST(self):
        db = MySQLdb.connect(host='localhost', user='root', passwd='lxb', db='Pedometer', charset='utf8')
        cursor = db.cursor()
        para = web.input()
        username = para['username']
        cursor.execute("""select * from user order by distance desc""")
        all_records = cursor.fetchall()
        i = 0
        result = []
        for record in all_records:
            temp = []
            i += 1
            if i > 10:
                break
            temp.append(str(record[0]))
            temp.append(int(float(record[7])))
            result.append(temp)
        result.sort(key=lambda x: x[1], reverse=True)
        if username not in (i[0] for i in result):
            cursor.execute("""select * from user where username = %s""", (username,))
            record = cursor.fetchone()
            distance = record[7]
            count = cursor.execute("""select * from user where distance > %s""", (str(distance),))
            temp = []
            temp.append(str(count))
            temp.append(int(float(distance)))
            result.append(temp)
        cursor.close()
        db.close()
        return result


class NewWeek:
    def POST(self):
        db = MySQLdb.connect(host='localhost', user='root', passwd='lxb', db='Pedometer', charset='utf8')
        cursor = db.cursor()
        para = web.input()
        username = para['username']
        plan = para['plan']
        cursor.execute("""update user set distance = %s""", ('0',))
        cursor.execute("""update user set plan = %s where username = %s""", (plan, username,))
        cursor.close()
        db.close()


if __name__ == '__main__':
    app = web.application(urls, globals())
    app.run()

