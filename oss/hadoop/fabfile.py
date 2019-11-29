from fabric.api import *

env.hosts = ['10.224.77.175', '10.224.77.178', '10.224.77.179']
env.user = "walter"
env.password = "tahoedev"


def freedisk(param='-h'):
    cmd = 'df ' + param
    run(cmd)


def listfile(folder='~'):
    cmd = 'ls -l ' + folder
    run(cmd)


@task
def hostinfo():
    run('uname -s')


# 1
@task
def setupssh():
    print("executing on %s as %s" % (env.host, env.user))
    sudo('chmod 0700 /root/.ssh')
    sudo('chmod 0600 /root/.ssh/id_rsa')
    sudo('touch /root/.ssh/authorized_key')
    sudo('chmod 0644 /root/.ssh/authorized_key')
    local('mkdir -p %s' % env.host)
    local_file = '%s/id_rsa.pub' % env.host
    remote_file = '/root/.ssh/id_rsa.pub'
    get(remote_file, local_file, use_sudo=True)


# 2
@task
def uploadpk():
    for aHost in env.hosts:
        local_file = '%s/id_rsa.pub' % aHost
        remote_id_file = '/tmp/id_rsa.pub_%s' % aHost
        put(local_file, remote_id_file)
        sudo('cat %s >> /root/.ssh/authorized_key' % remote_id_file)
        sudo('rm -f %s' % remote_id_file)


@task
def installpdsh():
    sudo("apt install -y pdsh")
    sudo("touch /root/.wcoll")
    sudo("echo 'wdm[1-3]' >> /root/.wcoll")

    sudo("touch /root/.bashrc")
    sudo("echo 'export PDSH_RCMD_TYPE=ssh' >> /root/.bashrc")
    sudo("echo 'export WCOLL=~/.wcoll' >> /root/.bashrc")


# 3
@task
def installjdk():
    sudo('apt install -y openjdk-8-jdk')


# 4
@task
def download_ambari():
    cmds = []
    cmds.append(
        'wget -O /etc/apt/sources.list.d/ambari.list http://public-repo-1.hortonworks.com/ambari/ubuntu14/2.x/updates/2.7.3.0/ambari.list')
    cmds.append('apt-key adv --recv-keys --keyserver keyserver.ubuntu.com B9733A7A07513CAD')
    cmds.append('apt-get update')
    cmds.append('apt-cache showpkg ambari-server')
    cmds.append('apt-cache showpkg ambari-agent')
    cmds.append('apt-cache showpkg ambari-metrics-assembly')
    for cmd in cmds:
        sudo(cmd)


# 5
@task
def install_ambari():
    sudo('apt-get install -y ambari-server')


@task
def setupenv():
    sudo('groupadd cdata && useradd -s /bin/bash -m -g cdata dmply')
    sudo('chmod +w /etc/sudoers')
    sudo('echo "dmply ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers')
    sudo('chmod -w /etc/sudoers')

    sudo('java -version')
