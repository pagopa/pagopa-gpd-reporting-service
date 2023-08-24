# Integration Tests
👀 Integration tests are in `integration-test/src/` folder. See there for more information.

## How run in local environment

To run the integration test from local environment, simply execute this commands:
```
yarn install && \
yarn test:${{ parameters.ENV }} ${{ secrets.API_SUBSCRIPTION_KEY }}
```

## How run on Docker 🐳

To run the integration tests on docker, you can run from this directory the script:


``` shell
sh ./run_integration_test.sh <local|dev|uat> <api-subscription-key>
```


ℹ️ _Note_: for **PagoPa ACR** is **required** the login `az acr login -n <acr-name>`

If you use dev, uat or prod **you test the images on Azure ACR**

---
💻 If you want to test your local branch,
``` shell
sh ./run_integration_test.sh local <api-subscription-key>
```