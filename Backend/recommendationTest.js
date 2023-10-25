const recommendation = require('./recommendation.js');
console.log('hello');

const event1 = {
  start: new Date('2021-05-01T12:00:00'),
  end: new Date('2021-05-01T13:00:00'),
  lat: 49.265683403716245,
  lng: -123.23694585557418,
};

const event2 = {
  start: new Date('2021-05-01T14:00:00'),
  end: new Date('2021-05-01T15:00:00'),
  lat: 49.26608946747301,
  lng: -123.25509904622521,
};

recommendation(event1, event2).then((result) => {
  if (result.length == 0) {
    console.log('no result');
  }
  for (let i = 0; i < result.length; i++) {
    console.log(result[i]);
  }
});
