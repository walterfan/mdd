
# precondition

* download redis package and unpack it
* install python3 and fabric3
 
```
    apt install fabric3
```

* change the redis_path in fabfile.py

* execute the following command to create redis cluster


```
    fab generate_config
    fab start_redis
    fab create_redis_cluster
    fab redis_cli:info
```

