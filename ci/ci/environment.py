import os

from gear.cloud_config import get_global_config

global_config = get_global_config()

CLOUD = global_config['cloud']

DOCKER_PREFIX = os.environ['HAIL_DOCKER_PREFIX']
DOCKER_ROOT_IMAGE = os.environ['HAIL_DOCKER_ROOT_IMAGE']
DOMAIN = os.environ['HAIL_DOMAIN']
KUBERNETES_SERVER_URL = os.environ['KUBERNETES_SERVER_URL']
CI_UTILS_IMAGE = os.environ['HAIL_CI_UTILS_IMAGE']
BUILDKIT_IMAGE = os.environ['HAIL_BUILDKIT_IMAGE']
DEFAULT_NAMESPACE = os.environ['HAIL_DEFAULT_NAMESPACE']
BUCKET = os.environ['HAIL_CI_BUCKET_NAME']
