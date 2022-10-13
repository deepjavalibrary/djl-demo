import torch
from torch import autocast
from diffusers import StableDiffusionPipeline,StableDiffusionImg2ImgPipeline
from djl_python import Input, Output
import io
from PIL import Image
from io import BytesIO
import requests

textPipe = StableDiffusionPipeline.from_pretrained("/opt/ml/model/stable-diffusion-v1-4",revision="fp16", torch_dtype=torch.float16).to("cuda")
imgPipe = StableDiffusionImg2ImgPipeline.from_pretrained("/opt/ml/model/stable-diffusion-v1-4",revision="fp16", torch_dtype=torch.float16).to("cuda")
#url = "https://raw.githubusercontent.com/CompVis/stable-diffusion/main/assets/stable-samples/img2img/sketch-mountains-input.jpg"
#response = requests.get(url)
def handle(inputs: Input) -> Output:
    global textPipe
    global imgPipe
    if inputs.is_empty():
        return None
    content_type = inputs.get_property("content-type")
    if content_type == "application/json":
        param = inputs.get_as_json()
        prompt =param.get('prompt')
        height =param.get('height')
        width =param.get('width')
        steps =param.get('steps')
        scale =param.get('scale')
        with torch.autocast("cuda"):
            image = textPipe(prompt,guidance_scale=scale, num_inference_steps=steps,height=height, width=width)["sample"][0]
            buf = io.BytesIO()
            image.save(buf, format='PNG')
            byte_im = buf.getvalue()
            return Output().add(byte_im).add_property("content-type","image/png")
    elif content_type is not None and content_type.startswith("text/"):
        prompt =inputs.get_as_string()
        with torch.autocast("cuda"):
            image = textPipe(prompt)["sample"][0]
            buf = io.BytesIO()
            image.save(buf, format='PNG')
            byte_im = buf.getvalue()
            return Output().add(byte_im).add_property("content-type","image/png")
    else:
        init_image = Image.open(BytesIO(inputs.get_as_bytes())).convert("RGB")
        #init_image = Image.open(BytesIO(response.content)).convert("RGB")
        init_image = init_image.resize((768, 512))
        prompt =inputs.get_as_string(key="prompt")
        #prompt = "A fantasy landscape, trending on artstation"
        with torch.autocast("cuda"):
            image = imgPipe(prompt=prompt, init_image=init_image, strength=0.75, guidance_scale=7.5).images[0]
            buf = io.BytesIO()
            image.save(buf, format='PNG')
            byte_im = buf.getvalue()
            return Output().add(byte_im).add_property("content-type","image/png")
