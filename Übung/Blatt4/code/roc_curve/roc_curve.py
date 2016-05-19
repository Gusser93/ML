import matplotlib.pyplot as plt
import numpy as np


def plot_roc_curve(table):
    """
    plots roc curve
    :param table: list with lists with classification and score
    :return:
    """
    table.sort(key=lambda line: -line[1])
    points = table_to_roc(table)
    points_tuple = [(point[1], point[2]) for point in points]
    hull = upper_hull(points_tuple)
    plt.plot([point[1] for point in points], [point[2] for point in points], "o")
    for point in points:
        name = point[0]
        point = (point[1], point[2])
        plt.annotate(s=name, xy=point, xytext=(-10, 10), textcoords="offset points")

    plt.plot([p[0] for p in hull], [p[1] for p in hull], 'k-')
    plt.suptitle("  ROC-Curve", horizontalalignment='center', fontsize=16)
    plt.ylabel("True positive rate")
    plt.xlabel("False positive rate")
    auc = np.trapz([point[2] for point in points], [point[1] for point in points])
    auc = str(auc)
    # plt.text(0.5, 0, "AUC = " + auc, horizontalalignment='center', verticalalignment="bottom")
    plt.title("AUC = " + auc, horizontalalignment='center', fontsize=12)

def upper_hull(points):
    points = sorted(set(points))

    if len(points) <= 1:
        return points

    upper = []
    for p in reversed(points):
        while len(upper) >= 2 and cross(upper[-2], upper[-1], p) <= 0:
            upper.pop()
        upper.append(p)

    return upper


def cross(o, a, b):
    return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])


def table_to_roc(table):
    p_percentage = 1.0 / number_of(table, "p")
    n_percentage = 1.0 / number_of(table, "n")

    points = [["infinity", 0, 0]]

    for line in table:
        score = line[1]
        classification = line[0]
        last_point = points[-1]
        if classification is "p":
            points += [[score, last_point[1], last_point[2] + p_percentage]]
        else:
            points += [[score, last_point[1] + n_percentage, last_point[2]]]

    return points


def number_of(table, value):
    count = 0
    for line in table:
        if line[0] is value:
            count += 1
    return count


if __name__ == "__main__":
    number1 = [["p", 0.95], ["n", 0.85], ["p", 0.78], ["p", 0.66], ["n", 0.60], ["p", 0.55], ["n", 0.53], ["n", 0.52],
               ["n", 0.51], ["p", 0.40]]
    plot_roc_curve(number1)
    plt.plot((0, 1), (0, 1), "k--")
    plt.show()
