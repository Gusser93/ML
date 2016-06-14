import numpy as np

X = np.matrix([[3.437, 5.791, 3.268, 10.649],\
		[12.801, 4.558, 5.751, 14.375],\
		[6.136, 6.223, 15.175, 2.811],\
		[11.685, 3.212, 0.639, 0.964],\
		[5.733, 3.220, 0.534, 2.052],\
		[3.021, 4.348, 0.839, 2.356],\
		[1.689, 0.634, 0.318, 2.209],\
		[2.339, 1.895, 0.610, 0.605],\
		[1.025, 0.834, 0.734, 2.825],\
		[2.936, 1.419, 0.331, 0.231],\
		[5.049, 4.195, 1.589, 1.957],\
		[1.693, 3.602, 0.837, 1.582],\
		[1.187, 2.679, 0.459, 18.837],\
		[9.730, 3.951, 3.780, 0.524],\
		[14.325, 4.300, 10.781, 36.863],\
		[7.737, 9.043, 1.394, 1.524],\
		[7.538, 4.538, 2.565, 5.109],\
		[10.211, 4.994, 3.081, 3.681],\
		[8.697, 3.005, 1.378, 3.338]])

Y = np.matrix([27.698, 57.634, 47.172, 49.295,\
		24.115, 33.612, 9.512, 14.755,\
		10.570, 15.394, 27.843, 17.717,\
		20.253, 37.465, 101.334, 47.427,\
		35.944, 45.945, 46.890]).transpose() 

# create fake input for constant term X = np.insert(X, O, values=1, axis=1) 
# ridge regression
Xtranspose = X.transpose()
I = np.matrix(np.identity(len(X[0].transpose())))
ridge = np.multiply(0.1, I)
left_term = Xtranspose * X + I 

# compute w = (X^T * X)^(-1) * (X^T * X)
solution = np.linalg.inv(left_term) * (Xtranspose * Y)
print(solution) 

# predict values of training data
Y = Y.transpose()[0].tolist()[0]
w = np.array(solution).transpose()[0].tolist()
predictions = []
 
for x in X:
    inst = np.array(x)[0].tolist()
    y = [w_i * v_i for w_i, v_i in zip(w, inst)]
    predictions.append(sum(y))

# output
output_format = "{0:8.2f} {1:8.2f}"
for y, p in zip(Y, predictions):
	print(output_format.format(y, p)) 
