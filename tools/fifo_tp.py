import sys
sys.path.append("./output/")

pnum = 9
total = 0

for i in range(pnum):
	filename = "./output/proc0" + str(i+1) + ".output"
	with open(filename,"r") as f:
		data = f.readlines()
		print(len(data))
		total += len(data)

print('average: ', total / pnum)