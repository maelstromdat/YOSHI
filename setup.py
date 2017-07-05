from distutils.core import setup

requirements = [
    "jinja2",
    "pip",
    "PyQt4"
]

setup(
    name="YoshiViz",
    packages=["YoshiViz"],
    version="0.0.1",
    description="Visualisation layer for Yoshi",
    requires=requirements
)
