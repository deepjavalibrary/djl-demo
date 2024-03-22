import io
import re

NEWLINE = re.compile(r'\\n')  
DOUBLE_NEWLINE = re.compile(r'\\n\\n')

class LineIterator:
    """
    A helper class for parsing the byte stream from Llama 2 model inferenced with LMI Container. 
    
    The output of the model will be in the following repetetive but incremental format:
    ```
    b'{"generated_text": "'
    b'lo from L"'
    b'LM \\n\\n'
    b'How are you?"}'
    ...

    For each iteration, we just read the incremental part and seek for the new position for the next iteration till the end of the line.

    """
    
    def __init__(self, stream):
        self.byte_iterator = iter(stream)
        self.buffer = io.BytesIO()
        self.read_pos = 0

    def __iter__(self):
        return self

    def __next__(self):
        start_sequence = b'{"generated_text": "'
        stop_sequence = b'"}'
        new_line = '\n'
        double_new_line = '\n\n'
        while True:
            self.buffer.seek(self.read_pos)
            line = self.buffer.readline()
            if line:
                self.read_pos += len(line)
                if line.startswith(start_sequence):# in :
                    line = line.lstrip(start_sequence)
                
                if line.endswith(stop_sequence):
                    line =line.rstrip(stop_sequence)
                line = line.decode('utf-8')
                line = NEWLINE.sub(new_line, line)
                line = DOUBLE_NEWLINE.sub(double_new_line, line)
                return line
            try:
                chunk = next(self.byte_iterator)
            except StopIteration:
                if self.read_pos < self.buffer.getbuffer().nbytes:
                    continue
                raise
            if 'PayloadPart' not in chunk:
                print('Unknown event type:' + chunk)
                continue
            self.buffer.seek(0, io.SEEK_END)
            self.buffer.write(chunk['PayloadPart']['Bytes'])