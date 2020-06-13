from fabric.api import *
from fabric.api import settings
from fabric.context_managers import *
from fabric.contrib.console import confirm
import os, subprocess

redis_path = '/home/walter/package/redis-5.0.8/src'
redis_config = '''daemonize yes
bind 0.0.0.0
port 9001
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes

'''
@task
def clean_config():
    for port in range(9001,9007):
        local("rm -rf {}".format(port))

@task
def write_config(file_path, port):
    config_content = redis_config.replace('9001', str(port))
    with open(file_path, "w") as fp:
        fp.write(config_content)
@task
def generate_config():
    for port in range(9001,9007):
        local("mkdir -p {}".format(port))
        config_file = '{}/redis.conf'.format(port)
        print("write {}".format(config_file))
        write_config(config_file, port)
@task
def start_redis():
    for folder in range(9001,9007):
        with lcd(str(folder)):
            local("{}/redis-server ./redis.conf".format(redis_path))

@task
def stop_redis():
    cmd = redis_path + "/redis-cli -p {} shutdown nosave"
    for port in range(9001,9007):
        local(cmd.format(port))
@task
def kill_redis():
    cmd = "ps -efw --width 1024|grep redis-server |grep -v grep|awk '{print $2}"
    pids = subprocess.check_output(cmd, shell=True)
    print(pids)
    with settings(warn_only=True):
        for pid in pids.decode("utf-8").split('\n'):
            local("kill -9 {}".format(pid))
@task
def check_ports_mac():
    with settings(warn_only=True):
        for port in range(9001,9007):
            local("lsof -nP -iTCP:{} | grep LISTEN".format(port))
@task
def check_redis():
    cmd = "ps -ef|grep redis-server |grep -v grep"
    with settings(warn_only=True):
        local(cmd)

@task
def create_redis_cluster():
    cmd = redis_path + "/redis-cli --cluster create {} {}"
    host_and_ports = ""
    for port in range(9001,9007):
            host_and_ports = host_and_ports + "0.0.0.0:{} ".format(port)
    option = "--cluster-replicas 1"
    local(cmd.format(host_and_ports, option))

@task
def redis_cli(command=''):
    if command:
        local(redis_path + "/redis-cli -c -p 9001 %s" % command)
    else:
        local(redis_path + "/redis-cli -p 9001")
