import numpy as np
import urllib3
from tempfile import TemporaryFile

http = urllib3.PoolManager()

data = np.zeros((1, 3, 224, 224), dtype=np.float32)
outfile = TemporaryFile()
np.savez(outfile, data)
_ = outfile.seek(0)

response = http.request(
    'POST',
    'http://localhost:8080/predictions/resnet',
    headers={'Content-Type': 'tensor/npz', "Accept": "tensor/npz"},
    body=outfile.read()
)

print(np.load(response.data))
