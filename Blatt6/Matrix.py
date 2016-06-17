class Lr_classiier:
    w = [[]]

    def __init__(self):
        w = [[]]

    def train(self, X, Y):
        self.w = matrix_multiplication(inverse(matrix_multiplication(transpose(X), X)),
                                  matrix_multiplication(transpose(X), Y))

    def classifie(self, x):
        if len(self.w) == 0:
            return
        return matrix_multiplication(transpose(self.w), x)


def matrix_multiplication(A, B):
    C = []
    l = len(A)
    m = len(B)
    n = len(B[-1])

    for i in range(l):
        c_i = []
        for k in range(n):
            c_i_k = 0
            for j in range(m):
                c_i_k += A[i][j] * B[j][k]
            c_i.append(c_i_k)
        C.append(c_i)
    return C


def id_matrix(size):
    id = []
    for i in range(size):
        id.append([0]*size)
    for i in range(size):
        id[i][i] = 1
    return id


def transpose(A):
    A_t = []
    for j in range(len(A[-1])):
        a_j = []
        for i in range(len(A)):
            a_j_i = A[i][j]
            a_j.append(a_j_i)
        A_t.append(a_j)
    return A_t


def inverse(A):
    return gj_solver(A)


def gj_solver(A, b=False):
    if b is not False:
        if len(A) != len(b):
            print "Wrong"
            return
        Ab = A[:]
        Ab.append(b)
        m = transpose(Ab)
    else:
        ii = id_matrix(len(A))
        Aa = A[:]
        for col in range(len(ii)):
            Aa.append(ii[col])
        tAa = transpose(Aa)
        m = tAa[:]

    (eqns, colrange, augCol) = (len(A), len(A), len(m[0]))

    for col in range(0, colrange):
        bigrow = col
        for row in range(col + 1, colrange):
            if abs(m[row][col]) > abs(m[bigrow][col]):
                bigrow = row
                (m[col], m[bigrow]) = (m[bigrow], m[col])
    # print "m is " + str(m)

    for rrcol in range(0, colrange):
        for rr in range(rrcol + 1, eqns):
            cc = -(float(m[rr][rrcol])) / float(m[rrcol][rrcol])
            for j in range(augCol):
                m[rr][j] += cc * m[rrcol][j]

    for rb in reversed(range(eqns)):
        if m[rb][rb] == 0:
            if m[rb][augCol - 1] == 0:
                continue
            else:
                print "system is inconsistent"
                return
        else:
            for backCol in reversed(range(rb, augCol)):
                m[rb][backCol] = float(m[rb][backCol]) / float(m[rb][rb])

            if not (rb == 0):
                for kup in reversed(range(rb)):
                    for kleft in reversed(range(rb, augCol)):
                        kk = -float(m[kup][rb]) / float(m[rb][rb])
                        m[kup][kleft] += kk * float(m[rb][kleft])

    if b is not False:
        return m
    else:
        m_out = []
        for row in range(len(m)):
            r_out = []
            for col in range(augCol / 2, augCol):
                r_out.append(m[row][col])
            m_out.append(r_out)
        return m_out


if __name__ == "__main__":
    X = [
        [3.437, 5.791, 3.268, 10.649],
        [12.801, 4.558, 5.751, 14.375],
        [6.136, 6.223, 15.175, 2.811],
        [11.685, 3.212, 0.639, 0.964],
        [5.733, 3.220, 0.534, 2.052],
        [3.021, 4.348, 0.839, 2.356],
        [1.689, 0.634, 0.318, 2.209],
        [2.339, 1.895, 0.610, 0.605],
        [1.025, 0.834, 0.734, 2.825],
        [2.936, 1.419, 0.331, 0.231],
        [5.049, 4.195, 1.589, 1.957],
        [1.693, 3.602, 0.837, 1.582],
        [1.187, 2.679, 0.459, 18.837],
        [9.730, 3.951, 3.780, 0.524],
        [14.325, 4.300, 10.781, 36.863],
        [7.737, 9.043, 1.394, 1.524],
        [7.538, 4.538, 2.565, 5.109],
        [10.211, 4.994, 3.081, 3.681],
        [8.697, 3.005, 1.378, 3.338],
    ]

    Y = [
        [27.698],
        [57.634],
        [47.172],
        [49.295],
        [24.115],
        [33.612],
        [9.512],
        [14.755],
        [10.570],
        [15.394],
        [27.843],
        [17.717],
        [20.253],
        [37.465],
        [101.334],
        [47.427],
        [35.944],
        [45.945],
        [46.890]
    ]

    print "inverse"
    for row in inverse(matrix_multiplication(transpose(X), X)):
        print row

    print "X * Y"
    for row in matrix_multiplication(transpose(X), Y):
        print row

    print "w"
    classifier = Lr_classiier()
    classifier.train(X, Y)
    print classifier.w
