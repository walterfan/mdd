
"""
  
  Author: Walter (yafan@cisco.com)

"""

import os
import sys
import argparse


import mysql.connector


class MySQLClient(object):
    def __init__(self, username, password, hostname, dbname):
        self.username = username
        self.password = password
        self.hostname = hostname
        self.dbname = dbname

        self.dbconn = mysql.connector.connect(user=self.username,
                                              password=self.password,
                                              host=self.hostname,
                                              database=self.dbname)
        

    def execute_cmd(self, strCmd, isOutput=False, isClose=False):
        self.cursor = self.dbconn.cursor()
        print(strCmd)
        self.cursor.execute(strCmd)
        if(isOutput):
            for row in self.cursor:
                print(row)
        if(isClose):
            self.cursor.close()


    def create_db(self):
        if self.dbname != 'scheduler':
            cmd4db = 'create database scheduler  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'
            self.execute_cmd(cmd4db, False, True)
    
    def create_user(self):
           
        if self.username != 'walter':
            cmd4user = "create user 'walter'@'%' identified by 'pass1234'"
            self.execute_cmd(cmd4user, False, True)

    def grant_privilege(self):
        cmd4grant="grant all on {}.* to 'walter'@'%'".format(self.dbname)
        self.execute_cmd(cmd4grant, False, True)

    def query(self, sql):
        self.execute_cmd(sql, True, True)


    def execute_sql(self, filename):
        # Open and read the file as a single buffer
        fd = open(filename, 'r')
        sqlFile = fd.read()
        fd.close()

        # all SQL commands (split on ';')
        sqlCommands = sqlFile.split(';')

        # Execute every command from the input file
        for command in sqlCommands:
            # This will skip and report errors
            # For example, if the tables do not yet exist, this will skip over
            # the DROP TABLE commands
            try:
              if command.rstrip() != '':
                #print("execute: %s" % command)
                #self.cursor.execute(command)
                execute_cmd(command)
            except ValueError as msg:
                print("Command skipped: ", msg)
        self.cursor.close()

    def commit(self):
        self.dbconn.commit()

    def close(self):
        self.cursor.close()
        self.dbconn.close()


def main(arguments):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)


    parser.add_argument('--sqlfile', action='store', dest='sqlfile', help='do a route policy testing')
    parser.add_argument('--username', action='store', dest='username', help='specify the keyspace')
    parser.add_argument('--password', action='store', dest='password', help='specify the password')
    parser.add_argument('--hostname', action='store', dest='hostname', default='localhost', help='specify the host name')
    parser.add_argument('--dbname', action='store', dest='dbname', default='mysql', help='specify the db name')
    parser.add_argument('--command', action='store', dest='command', help='specify the command name')
    parser.add_argument('--sql', action='store', dest='sql', default='show tables', help='specify the sql')

    args = parser.parse_args()

    print(args)

    if(args.username and args.password):
        mysqlClient = MySQLClient(args.username, args.password, args.hostname, args.dbname)
        print("connected database")
        if args.command == 'execute_sql':
            mysqlClient.execute_sql(args.sqlfile)
        elif args.command == 'create_db':
            mysqlClient.create_db()
        elif args.command == 'create_user':
            mysqlClient.create_user()
        elif args.command == 'grant_privilege':
            mysqlClient.grant_privilege()
        elif args.command == 'query':
            mysqlClient.query(args.sql)
        else:
            print_usage()
        return
    print_usage()

def print_usage():
    print("Usage: python mysql-client.py --username=walter --password=*** --command=<command_name>")
    print("e.g.")
    print("python mysql-client.py --username=root --password=pass1234 --dbname=scheduler --command=query --sql='select * from QRTZ_BLOB_TRIGGERS limit 10'")
    print("python mysql-client.py --username=root --password=pass1234 --dbname=scheduler --command=query --sql='show tables'")
    print("python mysql-client.py --username=root --password=pass1234 --command=create_db")
    print("python mysql-client.py --username=root --password=pass1234 --command=create_user")
    print("python mysql-client.py --username=root --password=pass1234 --command=grant_privilege --dbname=scheduler")
    print("python mysql-client.py --username=walter --password=pass1234 --dbname=scheduler --command=execute_sql --sqlfile=src/main/resources/schema.sql")
 
if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))