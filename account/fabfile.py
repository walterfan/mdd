from fabric.api import local

def app_run():
    local("python account.py")


def install_deps():
    local("pip install -r requirements.txt")
