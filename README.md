# stellar-coyote


## Requirements
- java 23+ - this was built and tested using java 23, though 21+ should work and can be changed in build.gradle
- gradle - not required, the gradle wrapper will download the appropriate version
- docker - the app will start different `postgres`es for test and main as long as there is a working docker environment
- testcontainer reuse - optionally, run this command to allow postgres to stay running when spring is shutdown. This avoids running in the csv on each startup but the included dataset only takes 17 seconds. `echo testcontainers.reuse.enable=true > ~/.testcontainers.properties`
- javac `-parameters` compiler option. This will be on by default when run from gradle but i had to enable it in my freshly installed ide. it is required for spring to read parameter names in the controller.
  

## Checkout and run
```bash
git clone git@github.com:velocirawesome/stellar-coyote.git
cd stellar-coyote
./gradlew bootRun
```

## Usage
### List accounts
<code>curl 'http://localhost:8080/account'</code>

The response lists all accounts along with the count of transactions.

```json
[
    {
        "name": "Aaron Murray",
        "account": "376028110684021",
        "amount": 175
    },
    {
        "name": "Samuel Sandoval",
        "account": "30364087349027",
        "amount": 143
    },
 ```

### get current balance

<code>curl 'http://localhost:8080/account/3590736522064285/balance'</code>

The response shows us the `actualMoney` at the 'current<sup>*</sup>' time, along with the `predictedMoney`to show how closely the model fits at that point.
The R^2 shows us that 72% of the variance in the data can be accounted for by the model. ('now' is fixed, see limitations below)

```json
{
    "account": "3590736522064285",
    "r2": 0.721365080403839,
    "predictedMoney": "3752.61",
    "lookbackDays": 537,
    "pointInTimeDate": "2020-06-21",
    "actualMoney": "3336.24"
}
```

### get historical balance
<code>curl 'http://localhost:8080/account/3590736522064285/balance?offsetDays=-100'</code>

We can set a negative offsetDays to view the balance at a point in the past. (Offset days makes this easier to experiment with compared to entering dates).
Again we can see the value that the model would have predicted along with the same R2 as the same model could be used from the previous request.

```json
{
    "account": "3590736522064285",
    "r2": 0.721365080403839,
    "predictedMoney": "3053.03",
    "lookbackDays": 537,
    "pointInTimeDate": "2020-03-13",
    "actualMoney": "2639.43"
}
```

### get future balance
<code>curl 'http://localhost:8080/account/3590736522064285/balance?offsetDays=100'</code>

By specifying a positive offsetDays, we no longer see the `actualMoney` field and only the `predictedMoney` based on the model.
As before, the same mode is used and the r2 is the same.

```json
{
    "account": "3590736522064285",
    "r2": 0.721365080403839,
    "predictedMoney": "4452.19",
    "lookbackDays": 537,
    "pointInTimeDate": "2020-09-29"
}
```

### get future balance using specific lookback period
<code>curl 'http://localhost:8080/account/3590736522064285/balance?lookbackDays=10'</code>

`lookbackDays` is optional in any of these calls and sets the number of days of historical data that is used to train the model.
Here were can see that with a lookback period of only 10 days, the prediction is much worse than in the first example above.
The vastly different R2 gives away that new model was trained however the negative number tells us the fit is bad enough that we would have got a better answer by simply averaging the data points.

```json
{
    "account": "3590736522064285",
    "r2": -270.87589308891353,
    "predictedMoney": "4355.82",
    "lookbackDays": 10,
    "pointInTimeDate": "2020-06-21",
    "actualMoney": "3336.24"
}
```

## Dataset
I wanted a big enough dataset in terms of different account to justify loading the data into postgres and fetching on demand.

I started with the Kaggle Credit Card Transactions Dataset https://www.kaggle.com/datasets/priyamchoksi/credit-card-transactions-dataset which contains 1.3m transactions across ~400 users.

The csv conversion utility `CsvProcessor.java` strips out unwanted columns and adds a running total.
It inverts 50% of the transactions as this dataset represents all spending and no income.
I eventually trimmed down the dataset to every 10th row to make it small enough to commit to github.

## Tech stack and design decisions
Some general advice I often give on a new project is change only one major bit of your tech stack compared to what you used in the last service/project.

Here I stuck with my personal tried and tested as I was already going to be getting my head around Tribuo. 

- java
- spring webflux
- spring data r2dbc
- postgres
- tribuo

## Prediction Logic
Originally I started looking at jTableSaw with Smile, but then discovered the smile integration was unmaintained and wasn't published in mvn central.
Looking at what else tablesaw supported, I settled on Tribuo and decided that the extra data loading and visualisation provided by tablesaw were not need.

The algorithm is a linear regression using adaptive gradient. The default linear decent gradient wasn't producing a working model (kept returning NaN) but adaptive gradient produced usable results.

The implimentation was taken largely from the tribuo docs here: https://tribuo.org/learn/4.0/tutorials/regression-tribuo-v4.html with slight variation to load examples from the db rather than csv.

The other main fix required to get a meaningful mode was to offset the timestamps in the data so that zero represented the start of the dataset rather than unix epoch. Without this, even the perfect-gradient test was producing outlandingly poor results.
https://github.com/velocirawesome/stellar-coyote/blob/ff140e5866ff2a2f00f2d7f4c8870ce5a5e326e2/src/test/java/com/velocirawesome/stellarcoyote/TribuoModelTrainerTest.java#L19-L23

## limitations
### Time
As the dataset is fixed - new transactions do not occur after the initial import, the concept of `now` is fixed at 21st July 2020, which is the end of the dataset. There is a `javax.time.Clock` abstraction that allows the app to start with a fixed clock without the rest of the app needing to know this detail.
