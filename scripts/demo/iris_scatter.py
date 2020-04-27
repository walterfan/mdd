from sklearn.datasets import load_iris
import matplotlib.pyplot as plt

iris = load_iris()
features = iris.data.T


plt.figure(figsize=(12, 8))
plt.rcParams.update({'font.size': 22})

_ = plt.scatter(features[0], features[1], alpha=0.9,
            s=100*features[3], c=iris.target, cmap='viridis')
_ = plt.xlabel('花萼长度(cm)')
_ = plt.ylabel('花萼宽度(cm)')
_ = plt.colorbar() # show color scale

plt.show()

