from flask import Flask, request
import optuna

# curl localhost:5005/suggest
# curl -X POST http://localhost:5005/complete -H 'Content-Type: application/json' -d '{"trial_number":0, "value": 5}'
# curl localhost:5005/results

app = Flask(__name__)

study = optuna.create_study(study_name="test-cma-es",
                            # sampler=optuna.samplers.CmaEsSampler(),
                            storage="sqlite:///ent.db",
                            direction=optuna.study.StudyDirection.MAXIMIZE,
                            load_if_exists=True)


@app.route('/suggest', methods=['POST'])
def suggest():
    trial = study.ask()
    data = request.get_json()
    params = {}
    for hyper in data:
        if hyper['type'] == 'int':
            suggested = trial.suggest_int(hyper['name'], hyper['minValue'], hyper['maxValue'])
            params[hyper['name']] = suggested
        else:
            raise Exception('unknown type')
        print(hyper)
    print("new trial {}, suggesting hyperparameters {}".format(trial.number, params))
    return {'parameters': params, 'trial_number': trial.number}


@app.route('/complete', methods=['POST'])
def complete():
    data = request.get_json()
    trial_number = data['trial_number']
    value = data['value']
    print("trial number {} completed with value {}".format(trial_number, value))
    study.tell(trial_number, value)
    return 'OK'


@app.route('/results', methods=['GET'])
def results():
    result = "Best value: {} (params: {})\n".format(study.best_value, study.best_params)
    print(result)
    return "<p>" + result + "</p>"


def trials():
    n_trials = 30
    for i in range(n_trials):
        trial = study.ask()

        x = trial.suggest_float("x", -100, 100)
        a = trial.suggest_int("a", 2, 100)
        y = trial.suggest_categorical("y", [-1, 0, 1])
        acc = x ** 2 + y

        study.tell(trial.number, acc)


if __name__ == "__main__":

    # # initial configuration
    # study.enqueue_trial({"x": 0.01})
    app.run(host='0.0.0.0', port=5005, debug=True)

