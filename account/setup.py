from setuptools import find_packages, setup

setup(name="microservices",
      version="0.1",
      description="account management",
      author="Walter",
      platforms=["any"],
      license="BSD",
      packages=find_packages(),
      install_requires=["Flask==2.2.5", "requests==2.32.2", "wsgiref==0.1.2"],
      )
