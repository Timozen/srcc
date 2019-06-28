import numpy as np
import cv2

Tile_Size = 336

Border_Size = 20

def main():
	sr = cv2.imread("img_sr.jpg",cv2.IMREAD_COLOR)
	generateTileMap(sr,Tile_Size)


def generateTileMap(image, tile_size):
	height, width, channels = image.shape
	tile_count_x = int(width / tile_size)
	tile_count_y = int(height / tile_size)
	cell_type = np.dtype(Tile)
	tiles = np.empty([tile_count_x,tile_count_y],dtype = cell_type)
	
	for x in range(tile_count_x):
		for y in range(tile_count_y):
			tiles[x][y] = Tile(x,y)

def set_border_averages(image, tilemap):
	pass
	


class Tile:
	def __init__(self,xCoord,yCoord):
		self.x = xCoord
		self.y = yCoord
	right_average = [0,0,0]
	left_average = [0,0,0]
	up_average = [0,0,0]
	down_average = [0,0,0]
	RGBFactors = 0

	
if __name__ == '__main__':
    main()
	

	

