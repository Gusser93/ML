import ast
import matplotlib.pyplot as plt

from getopt import getopt, GetoptError

import sys


def read_args(argv):
    arg_dict = {}
    switches = {"xList": list, "yList": list, "title": str, "xLabel": str, "yLabel": str}
    short_form = "".join([x[0:3]+":" for x in switches])
    long_form = [x + "=" for x in switches]
    d = {x[0:3] + ":": "--" + x for x in switches}
    try:
        opts, args = getopt(argv, short_form, long_form)
    except GetoptError:
        print("bad arg")
        sys.exit(2)

    for opt, arg in opts:
        if opt[0:3]+":" in d:
            o = d[opt[0:3]+":"][2:]
        elif opt in d.values():
            o = opt[2:]
        else:
            o = ""

        # print(opt, arg, o)

        if o and arg:
            arg_dict[o] = ast.literal_eval(arg)

        if not o or not isinstance(arg_dict[o], switches[o]):
            print(opt, arg, " Error: bad arg")
            sys.exit(2)

    return arg_dict


def plot(xList, yList, xLabel, yLabel, title):
    plt.title(title)
    plt.xlabel(xLabel)
    plt.ylabel(yLabel)
    plt.plot(xList, yList, "k-")
    plt.savefig(title + ".eps")


if __name__ == "__main__":
    # print(sys.argv[1:])
    args = read_args(sys.argv[1:])
    plot(args["xList"], args["yList"], args["xLabel"], args["yLabel"], args["title"])
