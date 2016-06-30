import numpy as np
from scipy.spatial import Voronoi, voronoi_plot_2d
import matplotlib.pyplot as plt

points = np.array([[1, 2], [3, 1], [6, 3], [3, 4], [2, 6]])
vor = Voronoi(points)
voronoi_plot_2d(vor)
plt.show()




