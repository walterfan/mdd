
# steps

```
virtualenv venv
source venv/bin/activate

pip install -r requirements.txt

python account.py

```

# dependencies management

pip freeze > requirements.txt

pip install -r requirements.txt


# Encryption
encryption_suite = AES.new(default_key, AES.MODE_CBC, default_iv)
cipher_text = encryption_suite.encrypt("A really secret message. Not for prying eyes.")

# Decryption
decryption_suite = AES.new('This is a key123', AES.MODE_CBC, 'This is an IV456')
plain_text = decryption_suite.decrypt(cipher_text)

## example
from Crypto.Cipher import AES

default_key = "this is a key"
default_iv = "this is an IV456"
