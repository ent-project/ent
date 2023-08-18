from flask import Flask, request
import optuna
from datetime import datetime

# curl localhost:5005/suggest
# curl -X POST http://localhost:5005/complete -H 'Content-Type: application/json' -d '{"trial_number":0, "value": 5}'
# curl localhost:5005/results

# optuna-dashboard sqlite:///ent.db

app = Flask(__name__)

study: optuna.Study = None

@app.route('/study', methods=['POST'])
def create_study():
    global study
    data = request.get_json()
    name = data['name']
    do_create_study(name)
    return 'OK'


def do_create_study(name):
    global study
    study = optuna.create_study(study_name=name,
                                # sampler=optuna.samplers.CmaEsSampler(),
                                storage="sqlite:///ent.db",
                                direction=optuna.study.StudyDirection.MAXIMIZE,
                                load_if_exists=True)


@app.route('/suggest', methods=['POST'])
def suggest():
    global study
    if study is None:
        do_create_study(datetime.now().strftime('study-%Y-%m-%d'))

    trial = study.ask()
    data = request.get_json()
    params = {}
    for hyper in data:
        if hyper['type'] == 'int':
            suggested = trial.suggest_int(hyper['name'], hyper['minValue'], hyper['maxValue'])
        elif hyper['type'] == 'float':
            suggested = trial.suggest_float(hyper['name'], hyper['minValue'], hyper['maxValue'])
        else:
            raise Exception('unknown type')
        params[hyper['name']] = suggested
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

@app.route('/initialize', methods=['POST'])
def initialize():
    data = request.get_json()
    print("initializing parameters to be used in the next trial: {}".format(data))
    study.enqueue_trial(data)
    return 'OK'

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
    app.run(host='0.0.0.0', port=5005, debug=True)

