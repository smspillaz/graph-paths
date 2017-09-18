import random
import sys

size = int(sys.argv[1])
density = float(sys.argv[2])

mat = [[1 if random.random() > (1.0 - density) else 0 for j in range(0, size)] for i in range(0, size)]

for x, row in enumerate(mat):
    for y, cell in enumerate(row):
        if cell == 1:
            print(x)
            print(y)
