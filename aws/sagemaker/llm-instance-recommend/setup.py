#!/usr/bin/env python
#
# Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
# except in compliance with the License. A copy of the License is located at
#
# http://aws.amazon.com/apache2.0/
#
# or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for
# the specific language governing permissions and limitations under the License.

import setuptools.command.build_py
from setuptools import setup, find_packages

pkgs = find_packages(exclude='src')


def detect_version():
    with open("../../../gradle.properties", "r") as f:
        for line in f:
            if not line.startswith('#'):
                prop = line.split('=')
                if prop[0] == "djl_version":
                    return prop[1].strip()

    return None




class BuildPy(setuptools.command.build_py.build_py):

    def run(self):
        setuptools.command.build_py.build_py.run(self)


if __name__ == '__main__':
    version = detect_version()

    requirements = ['psutil', 'packaging', 'wheel', 'requests']

    test_requirements = []

    setup(name='lmi_recommender',
          version=version,
          description=
          'lmi_recommender is a toolkit that can do lmi configuration recommendations',
          author='Deep Java Library team',
          author_email='rubikon-dev@amazon.com',
          url='https://github.com/deepjavalibrary/djl.git',
          keywords='Large Model Inference',
          packages=pkgs,
          cmdclass={
              'build_py': BuildPy,
          },
          install_requires=requirements,
          include_package_data=True,
          license='Apache License Version 2.0')
