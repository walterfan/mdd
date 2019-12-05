from PIL import Image
import PIL.ImageOps      
import sys
import argparse

old_file = 'old_image.png'
new_file = 'new_image.png'

def invert_color(old_file, new_file):
	print("invert color from {}  to {}...".format(old_file, new_file))
	image = Image.open(old_file)
	if image.mode == 'RGBA':
	    r,g,b,a = image.split()
	    rgb_image = Image.merge('RGB', (r,g,b))

	    inverted_image = PIL.ImageOps.invert(rgb_image)

	    r2,g2,b2 = inverted_image.split()

	    final_transparent_image = Image.merge('RGBA', (r2,g2,b2,a))

	    final_transparent_image.save(new_file)

	else:
	    inverted_image = PIL.ImageOps.invert(image)
	    inverted_image.save(new_file)

	print("invert color done.")
	
if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument('-i', action='store', dest='input', help='specify input image')
    parser.add_argument('-o', action='store', dest='output', help='specify out image')


    args = parser.parse_args()

    if (args.input):
        old_file = args.input
    else:
    	print("Please specify -i at least, -h for usage")
    	sys.exit(1)

    if (args.output):
        new_file = args.output

    invert_color(old_file, new_file)
