# 1) build TFLite model to deploy in current android app - 5hrs
# 2) Gap detection - algorithm
# 3) 

import numpy as np
from itertools import count
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.collections import PatchCollection
import matplotlib
from matplotlib.animation import FuncAnimation

from shapely.geometry import Point, Polygon

import heapq
import math
from io import StringIO

plt.style.use('fivethirtyeight')
fig, ax = plt.subplots(1)

index = count()

num_objects = 10
width = 10
height = 10
margin = 5
speed = 5
whisker = 30

start = []
target = []
x_vals = []
y_vals = []
point_color = []
x_dir = []
y_dir = []

dx = [-1, -1, -1, 0, 0, 1, 1, 1]
dy = [-1, 0, 1, -1, 1, -1, 0, 1]


def show_tree(tree, total_width=60, fill=' '):
    """Pretty-print a tree.
    total_width depends on your input size"""
    output = StringIO()
    last_row = -1
    for i, n in enumerate(tree):
        if i:
            row = int(math.floor(math.log(i+1, 2)))
        else:
            row = 0
        if row != last_row:
            output.write('\n')
        columns = 2**row
        col_width = int(math.floor((total_width * 1.0) / columns))
        output.write(str(n).center(col_width, fill))
        last_row = row
    print (output.getvalue())
    print ('-' * total_width)
    return

def initialize():
	global start, target, x_vals, y_vals, point_color, x_dir, y_dir
	start = np.random.randint(low = -90, high = 90, size = 2)
	target = np.random.randint(low = -90, high = 90, size = 2)

	vector = target - start

	uni = np.random.uniform(low = 0.3, high = 1.2, size = num_objects)

	x_vals = start[0] + vector[0] * uni
	y_vals = start[1] + vector[1] * uni

	x_vals += np.random.randint(low = -40, high = 40, size = num_objects)
	y_vals += np.random.randint(low = -40, high = 40, size = num_objects)

	# x_vals = np.random.randint(low = -90, high = 90, size = num_objects)
	# y_vals = np.random.randint(low = -90, high = 90, size = num_objects)
	# point_color = np.random.uniform(low = 0, high = 1, size = (num_objects, 3))

	x1, y1 = start
	x2, y2 = target

	dx = float(x2 - x1)
	dy = float(y2 - y1)
	vector = np.asarray([dx, dy])
	norm = np.linalg.norm(vector)
	vector /= norm

	directionStuff = np.random.randint(low = -3, high = 3, size = num_objects) * 2
	x_dir = vector[0] * directionStuff
	y_dir = vector[1] * directionStuff
	x_dir = x_dir.astype('int32')
	y_dir = y_dir.astype('int32')
	print(x_dir)


def rotate_vector(normalized_vector, degree_angle):
	x, y = normalized_vector
	rad = math.radians(degree_angle)
	
	x_rotated = x * np.cos(rad) - y * np.sin(rad)
	y_rotated = y * np.cos(rad) + x * np.sin(rad)

	return np.asarray([x_rotated, y_rotated])


def heuristic(next_x, next_y, goal_x, goal_y):
	dx = float(goal_x - next_x)
	dy = float(goal_y - next_y)
	vector = np.asarray([dx, dy])
	norm = np.linalg.norm(vector)

	heuristic_value = norm * 20

	return heuristic_value

def AstarPath(rectangles):
	global start, target, dx, dy


	# CALCULATE NORMALIZED VECTOR
	x1, y1 = start
	x2, y2 = target

	vector = np.asarray([x2 - x1, y2 - y1])
	norm = np.linalg.norm(vector)
	if norm <= 5:
		initialize()
		return True

	# print("NORM: ", norm)
	

	# GENERATE VECTORS (-10, -5, 0, 5, 10 degree variants)
	normalized_vector = vector / norm

	# five_degree_vector = rotate_vector(normalized_vector, 5)
	# ten_degree_vector = rotate_vector(normalized_vector, 10)

	# negative_five_degree_vector = rotate_vector(normalized_vector, -5)
	# negative_ten_degree_vector = rotate_vector(normalized_vector, -10)

	# print(normalized_vector, five_degree_vector, ten_degree_vector, negative_five_degree_vector, negative_ten_degree_vector)

	frontier = []
	heapq.heappush(frontier, (0, x1, y1)) # f = g + h, g, x location, y location

	came_from = {(x1, y1): (x1, y1)}
	cost_so_far = {(x1, y1): 0}

	print(start, target)

	# set_unique = set()

	while frontier:
		current_cost, current_x, current_y = heapq.heappop(frontier) #current_cost includes heuristic, which is used for sorting but not actual path finding...

		## TESTING START
		# print (current_x, current_y)
		# if (current_x, current_y) in set_unique:
		# 	print (current_x, current_y)

		# set_unique.add((current_x, current_y))
		## TESTING END

		# print(current_x, current_y)

		if current_x == x2 and current_y == y2:
			# FIND PATH according to came_from (recursive backwards from list until reach x1, y1)
			break

		for x, y in zip(dx, dy): # neighbors
			next_x = current_x + x
			next_y = current_y + y

			next_cost = cost_so_far[(current_x, current_y)]
			# next_cost += edge_weight(current_x, current_y, next_x, next_y) + 3

			intersected = False

			for i in range(15): # 25 iterations ahead

				lookAheadX = next_x + x*i
				lookAheadY = next_y + y*i
				lookAheadP = Point(lookAheadX, lookAheadY) 

				for rectangle in rectangles:
					if rectangle.contains(lookAheadP):
						next_cost += 5000
						intersected = True
						break

				if intersected: break


			if (next_x, next_y) not in cost_so_far or next_cost < cost_so_far[(next_x, next_y)]:
				cost_so_far[(next_x, next_y)] = next_cost
				priority = next_cost + heuristic(next_x, next_y, x2, y2)
				heapq.heappush(frontier, (priority, next_x, next_y))
				came_from[(next_x, next_y)] = (current_x, current_y)

	# we now have a path to goal with cost_so_far:

	

	path = []
	current = (x2, y2)

	if cost_so_far[current] > 5000:
		return False

	while current[0] != x1 or current[1] != y1:
		path.append(current)
		current = came_from[current]

	path = path[::-1]

	# print("PATH: ",  path)

	## INSTEAD OF JUST SETTING PATH, USE BEST ONE FROM VECTOR? SO DO WE NEED TO EVEN CALCULATE ENTIRE PATH? 
	##   
	start = path[4]
	return True


	# while 

	# heapq.heappush(open, (0,'one', 1))
	# heapq.heappush(open, (1,'two', 11))
	# heapq.heappush(open, (1, 'two', 2))
	# heapq.heappush(open, (1, 'one', 3))
	# heapq.heappush(open, (1,'two', 3))
	# heapq.heappush(open, (1,'one', 4))
	# heapq.heappush(open, (1,'two', 5))
	# heapq.heappush(open, (1,'one', 1))

	# show_tree(open)
	# open = util.PriorityQueue()
	# open = util.PriorityQueue() #initialize the open list as a priority queue structure:
	# # (x, y, total_cost)
 #    metadata = util.PriorityQueue() #stores the path that was taken as an priority queue of path arrays

 #    closed = [] #this is set of all the nodes that we have visited
 #    pathTaken = [] #this is a list of paths taken from the start state to the goal state

 #    current = start

 #    while np.linalg.norm(target - start) > 5:
    	
 #    	if current not in closed:
 #    		closed.append(current)
 #    		for x, y in zip(dx, dy):
 #    			new_x = current + x
 #    			new_y = current + y
 #    			moveTo = np.array([new_x, new_y])

 #    			heuristic = np.linalg.norm(target - moveTo)

def generatePath(rectangles):
	global start, target
	x1, y1 = start
	x2, y2 = target

	dx = float(x2 - x1)
	dy = float(y2 - y1)
	vector = np.asarray([dx, dy])
	norm = np.linalg.norm(vector)

	if norm <= 5:
		initialize()
		return True

	vector /= norm

	# print(f'vector_norm = <{vector}>')

	path_intersected = False
	for i in range(whisker): # 30 iterations
		
		if path_intersected: break

		lookAheadX = x1 + vector[0]*i
		lookAheadY = y1 + vector[1]*i
		lookAheadP = Point(lookAheadX, lookAheadY) 

		for rectangle in rectangles:
			if rectangle.contains(lookAheadP):
				path_intersected = True
				break

	if path_intersected:
		# print(f'PATH INTERSECTED: {path_intersected}')
		return False
	else:
		movement = np.rint(vector * speed).astype('int32')
		start += movement
		return True



def randomness():

	normal_dist = np.random.normal(loc=0, scale=5, size=num_objects)
	return np.rint(normal_dist).astype('int32')

def animate(i):		
	global x_vals, y_vals, x_dir, y_dir

	x_vals += x_dir #randomness()
	y_vals += y_dir #randomness()

	patches = []
	rectangles = []

	for x, y in zip(x_vals, y_vals):
		patches.append(matplotlib.patches.Rectangle((x, y), width, height, linewidth=1, fill=False))
		bottom_left = (x, y)
		bottom_right = (x + width + margin, y)
		top_right = (x + width + margin, y + height + margin)
		top_left = (x, y + height + margin)
		rectangles.append(Polygon([bottom_left, bottom_right, top_right, top_left]))

	plt.cla()	

	plt.xlim(-100, 100)
	plt.ylim(-100, 100)
	
	# Create path
	isMoving = AstarPath(rectangles)

	ax.add_collection(PatchCollection(patches))

	if isMoving:
		ax.scatter(start[0], start[1], c = 'g', s=100)
	else:
		ax.scatter(start[0], start[1], c = 'r', s=100)

	ax.scatter(target[0], target[1], c = 'black', s=100)

initialize()

ani = FuncAnimation(plt.gcf(), animate, interval = 2000)

plt.tight_layout()
plt.show()
