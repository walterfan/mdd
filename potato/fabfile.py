from fabric.api import *
from fabric.context_managers import *
from fabric.contrib.console import confirm
import os, subprocess
from fabric.api import settings

local_path = os.path.dirname(os.path.abspath(__file__))
data_dir = local_path + "/data"

backend_service_ports={
"cassandra": "9042",
"elasticsearch": "9200 9300",
"influxdb": "8086",
"postgres": "5432",
"rabbitmq": "4369 5671 5672 15671 15672 25672",
"redis": "6379",
"riak": "8087 8098",
"kafka-zookeeper": "2181 9092"
}

need_print_cmd=True
only_display_cmd=False

docker_image_prefix="walterfan-"
docker_container_prefix="msa-"

restart_policy="--restart always"
jenkins_volume_mapping = "%s/jenkins:/var/jenkins_home" % data_dir
jenkins_container_name="jenkins"
#jenkins_image_name="walterfan-jenkins"
jenkins_image_name="jenkins/jenkins:lts"

def run_cmd(cmd, warnOnly=False):
	if(need_print_cmd):
		print(cmd)
	if not only_display_cmd:
		with settings(warn_only=warnOnly):
			local(cmd)
#----------------------------- jenkins --------------------------#

@task
def test():
	print(local_path)
	print(jenkins_volume_mapping)

@task
def jenkins_run(listen_port="8080"):
	cmd = "docker run %s -v %s -p %s:8080 -p 50000:50000 --name=%s -d %s" % (restart_policy, jenkins_volume_mapping, listen_port, jenkins_container_name, jenkins_image_name)
	run_cmd(cmd)


@task
def jenkins_start():
	cmd = "docker start %s" % jenkins_container_name
	run_cmd(cmd)

@task
def jenkins_stop():
	cmd = "docker stop %s" % jenkins_container_name
	local(cmd)
	#cmd = "docker cp jenkins-container:/var/log/jenkins/jenkins.log jenkins.log"
	#local(cmd)

@task
def jenkins_remove():
	docker_remove(jenkins_container_name)

@task
def jenkins_commit(message):
	cmd = "docker commit -m \"%s\" %s walterfan/jenkins:1.0" % (message, jenkins_container_name)

@task
def jenkins_check():
	cmd = "docker exec %s ps -ef | grep java" % jenkins_container_name
	print(cmd)
	local(cmd)

	cmd = "docker exec %s cat /var/jenkins_home/secrets/initialAdminPassword" % jenkins_container_name
	print(cmd)
	local(cmd)

#-----------------------------build application --------------------------#

@task
def redeploy():
	stop_services()
	for container_name in ["potato_potato","potato_web","potato_scheduler"]:
		cmd = "docker rmi {}".format(container_name)
		run_cmd(cmd, True)
	run_cmd("mvn clean package -DskipTests=true")
	
	run_cmd("docker-compose up --force-recreate -d")

@task
def init_db():
	run_cmd("docker-compose up -d mysqldb")
	run_cmd("python ./potato-scheduler/mysql-client.py --username=root --password=pass1234 --command=grant_privilege  --dbname=scheduler")
	run_cmd("python ./potato-scheduler/mysql-client.py --username=root --password=pass1234 --command=grant_privilege  --dbname=potato")
	run_cmd("python ./potato-scheduler/mysql-client.py --username=root --password=pass1234 --command=grant_privilege  --dbname=userservice")

@task
def package():
	run_cmd("docker-compose up -d mysqldb")
	run_cmd("mvn clean package -Dmaven.test.skip=true")

@task
def registry_build():
	run_cmd("docker build -t walterfan/potato-registry:0.0.1 ./potato-registry")
	run_cmd("docker tag walterfan/potato-registry:0.0.1 walterfan/potato-registry:latest")


@task
def potato_build():
	run_cmd("docker build -t walterfan/potato-app:0.0.1 ./potato-server")
	run_cmd("docker tag walterfan/potato-app:0.0.1 walterfan/potato-app:latest")

@task
def potato_web_build():

	run_cmd("docker build -t walterfan/potato-web:0.0.1 ./potato-web")
	run_cmd("docker tag walterfan/potato-web:0.0.1 walterfan/potato-web:latest")
	

@task
def potato_web_deploy():
	run_cmd("docker-compose stop web")
	run_cmd("mvn clean package -DskipTests=true")
	potato_web_build()
	run_cmd("docker-compose start web")


@task
def scheduler_build():
	run_cmd("docker build -t walterfan/potato-scheduler:0.0.1 ./potato-scheduler")
	run_cmd("docker tag walterfan/potato-scheduler:0.0.1 walterfan/potato-scheduler:latest")


@task
def scheduler_dbinit():
	with lcd("potato-scheduler"):
		run_cmd("python mysql-client.py --username=walter --password=pass1234 --sqlfile=./src/main/resources/schema.sql")

#-----------------------------grafana influx --------------------------#
@task
def graflux_build():
	cmd = "docker build --tag %s docker/%s" % ("graflux", "graflux")
	run_cmd(cmd)

@task
def graflux_start():
	grafana_port = 3000
	influx_api_port = 8086
	influx_web_port = 8083
	cmd = "docker run --name local-graflux -d -p %d:3000 -p %d:8086 -p %d:8083 graflux" % (grafana_port, influx_api_port, influx_web_port)
	print(cmd)
	local(cmd)

@task
def influx():
	"""
	execute the influx command in graflux docker
	"""
	cmd = "docker exec -it local-graflux influx"
	run_cmd(cmd)

@task
def graflux_bash():
	"""
	execute the /bin/bash in graflux docker
	"""
	cmd = "docker exec -it local-graflux /bin/bash"
	run_cmd(cmd)

@task
def graflux_stop():
	#cmd = "docker stop local-graflux"
	docker_remove("local-graflux")

@task
def redis_cli():
	cmd = "docker exec -it local-redis redis-cli"
	local(cmd)
@task
def redis_bash():
	cmd = "docker exec -it local-redis /bin/bash"
	local(cmd)


@task
def cassandra_cql(cql=''):
	cmd = "docker exec -it local-cassandra /usr/bin/cqlsh "
	if cql:
		cmd = cmd + " -e '%s'" % cql
	local(cmd)

@task
def mysql_cli(usr='root'):
	cmd = "docker exec -it local-mysql /usr/bin/mysql -u %s -p" % usr
	local(cmd)

@task
def mysql_bash():
	cmd = "docker exec -it local-mysql /bin/bash"
	local(cmd)


@task
def start_mysql():
	cmd = "docker-compose up -d mysqldb"
	run_cmd(cmd)

@task
def start_services():
	cmd = "docker-compose up -d"
	run_cmd(cmd)

@task
def stop_services():
	cmd = "docker-compose down -v"
	run_cmd(cmd)
#----------------------------- general commands ---
def get_container_id(container_name):
	str_filter = "-aqf name=%s" % container_name
	arr_cmd = ["docker", "ps", str_filter]
	container_id = subprocess.check_output(arr_cmd).strip()
	return container_id

@task
def docker_rename(old_name, new_name):
	cmd = "docker tag %s %s" % (old_name, new_name)
	run_cmd(cmd)


@task
def docker_build(service_name="local-tomcat"):
	docker_image_name = docker_image_prefix + service_name
	cmd = "docker build --tag %s docker/%s" % (docker_image_name, service_name)
	run_cmd(cmd)


@task
def docker_run(service_name="local-tomcat", volume_args="-v /workspace:/workspace"):
	port_args = get_port_args(service_name)

	docker_container_name = docker_container_prefix + service_name
	docker_image_name = docker_image_prefix + service_name

	cmd = "docker run %s %s %s -d --name %s %s" % (restart_policy, volume_args, port_args, docker_container_name, docker_image_name)
	run_cmd(cmd)

@task
def docker_stop(container_name="local-tomcat"):
	cmd = "docker stop %s" % (container_name)
	run_cmd(cmd)

@task
def docker_list():
	cmd = "docker-compose ps"
	run_cmd(cmd)

@task
def docker_exec(container_name="local-tomcat", instruction="/bin/bash"):

	instruction = "/bin/bash"
	cmd = "docker exec -it %s %s" % (container_name, 	instruction)
	run_cmd(cmd)

@task
def docker_remove(container_name="kanban"):
	cmd1 = "docker kill %s|| true" % container_name
	run_cmd(cmd1)

	cmd2 = "docker rm -v %s || true" % container_name
	run_cmd(cmd2)

@task
def docker_commit(container_id, image_name, message=""):
	cmd = "docker commit -m \"%s\" %s %s" % (message, container_id, image_name)
	run_cmd(cmd)

@task
def docker_install():
	#cmd  ="brew remove docker && brew upgrade"
	cmd = "brew cask install docker && open /Applications/Docker.app"
	run_cmd(cmd)

@task
def help():
	print("examples:\tfab docker_run:cassandra,\"-v /home/walter:/workspace\" ")
