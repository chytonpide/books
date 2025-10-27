const foods = ['apple', 'banana', 'banana', 'mango'];

let filterFood = foods.filter((food) => {
  return food === 'banana'
});
let findFood = foods.find((food) => {
  return food === 'banana'
});

console.log(filterFood); // 조건을 만족하는 요소의 배열이 리턴된다.
console.log(findFood); // 조건을 만족하는 요소 하나가 리턴된다.
