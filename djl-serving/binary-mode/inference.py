import numpy as np
import urllib3
import io

http = urllib3.PoolManager()

data = np.zeros((1, 3, 224, 224), dtype=np.float32)

memory_file = io.BytesIO()
np.savez(memory_file, data)
memory_file.seek(0)

response = http.request(
    'POST',
    'http://localhost:8080/predictions/resnet',
    headers={'Content-Type': 'tensor/npz', "Accept": "tensor/npz"},
    body=memory_file.read()
)

print(f"Response content-Type: {response.headers['content-type']}")
list = np.load(io.BytesIO(response.data))
output = list["arr_0"]
print(f"Output shape: {output.shape}")
