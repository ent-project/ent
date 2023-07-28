

## Hyperparameter optimization

[Optuna](https://optuna.org/) is used for hyperparameter optimization.

The optimizer is configured in the `hpo` subdirectory/module.

It is recommended to set up a virtual environment for python and use the [`requirements.txt`](hpo/requirements.txt) file
to install the python dependencies.

A server running Optuna is started like this:
```bash
(ent/hpo)$ python main.py
```

You can query the server for a new set of hyperparameters and submit the result of the trial via a REST interface.

This API is used by the Java-modules, but sending the requests by hand would look like this:

```bash
$ curl localhost:5005/suggest
# => some json output
$ curl -X POST http://localhost:5005/complete -H 'Content-Type: application/json' -d '{"trial_number":0, "value": 5}'
```

To fetch the results of the optimization:
```bash
$ curl localhost:5005/results
```



### Optuna dashboard

There is a very useful dashboard to visualize the optimization.
It is started, as [documented](https://optuna-dashboard.readthedocs.io/en/latest/) by the Optuna project:
```bash
(ent/hpo)$ optuna-dashboard --port 8989 sqlite:///ent.db
```



