import os

import SearchUtil

logger = SearchUtil.create_logger(__name__)

# refer to https://pyyaml.org/wiki/PyYAMLDocumentation
from yaml import load, dump

try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper


def get_home_path():
    return os.getenv("ELS_TOOL_HOME")


class YamlConfig:
    def __init__(self, yaml_file=get_home_path() + "/config/env_config.yml"):
        self.config_file = yaml_file
        self.config_data = self.read_config(yaml_file)

    def read_config(self, yaml_file):
        f = open(yaml_file, 'r', encoding='UTF-8')
        config_data = load(f)
        f.close()
        return config_data

    def get_config(self):
        return self.config_data

    def __str__(self):
        return dump(self.config_data, Dumper=Dumper)


class EnvConfig(YamlConfig):
    def get_els_urls(self, category='Potato', env='LAB'):
        urls = self.config_data.get(category, {}).get(env, {}).get('ELS_URL')
        return urls


class QueryConfig(YamlConfig):
    def get_query_string(self, category, name, params={}):
        return self.config_data.get(category, {}).get(name)


class AlertConfig(YamlConfig):
    def get_alert_config_items(self):
        if (not self.config_data):
            print("Found nothing in %s" % self.config_file)
            return []
        return self.config_data.items()

    def get_alert_config_item(self, itemName):
        return self.config_data.get(itemName)
