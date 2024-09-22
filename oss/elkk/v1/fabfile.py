from fabric.api import *
from fabric.context_managers import *
from fabric.contrib.console import confirm

@task
def hostinfo(param='-a'):
    cmd = 'uname ' + param
    run(cmd)

@task
def copypk():
    local_file="~/.ssh/id_rsa.pub"
    remote_file="/tmp/id_rsa.pub"
    put(local_file, remote_file)
    run("mkdir -p ~/.ssh")
    run("touch ~/.ssh/authorized_keys")
    run("cat /tmp/id_rsa.pub >> ~/.ssh/authorized_keys")
