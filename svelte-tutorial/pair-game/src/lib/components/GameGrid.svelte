<script>
  import {score, round, time, initTime, initScore, initRound} from "../../store/store.js";
  import Modal from "./Modal.svelte";

  export let page = "game";


  const card_data = [
    {
      id: 0,
      name: 'html',
      imgUrl: 'images/html.png',
    },
    {
      id: 1,
      name: 'css',
      imgUrl: 'images/css.png',
    },
    {
      id: 2,
      name: 'js',
      imgUrl: 'images/js.png',
    },
    {
      id: 3,
      name: 'react',
      imgUrl: 'images/react.png',
    },
    {
      id: 4,
      name: 'vue',
      imgUrl: 'images/vue.png',
    },
    {
      id: 5,
      name: 'svelte',
      imgUrl: 'images/svelte.png',
    },
    {
      id: 6,
      name: 'sass',
      imgUrl: 'images/sass.png',
    },
    {
      id: 7,
      name: 'github',
      imgUrl: 'images/github.png',
    },
    {
      id: 8,
      name: 'quest',
      imgUrl: 'images/quest.png',
    },
  ]

  // 카드 목록
  // id:카드번호, flipped: 뒤집혀진 상태, matched: 매칭된 상태
  let cards = [
    {id: 0, flipped: false, matched: false},
    {id: 0, flipped: false, matched: false},
    {id: 1, flipped: false, matched: false},
    {id: 1, flipped: false, matched: false},
    {id: 2, flipped: false, matched: false},
    {id: 2, flipped: false, matched: false},
    {id: 3, flipped: false, matched: false},
    {id: 3, flipped: false, matched: false},
    {id: 4, flipped: false, matched: false},
    {id: 4, flipped: false, matched: false},
    {id: 5, flipped: false, matched: false},
    {id: 5, flipped: false, matched: false},
    {id: 6, flipped: false, matched: false},
    {id: 6, flipped: false, matched: false},
    {id: 7, flipped: false, matched: false},
    {id: 7, flipped: false, matched: false},
  ]

  function replayGame() {
    initScore();
    initTime();
    resetGame();
  }

  function startFirstGame() {
    initScore();
    initRound();
    initTime();
    resetGame();
  }

  function resetGame() {
    cards.forEach(card => {
      card.flipped = false;
      card.matched = false;
    })

    gameCleared = false;
    gameOvered = false;
    shuffle();
    startTimer();
  }

  function nextGame() {
    resetGame();
    $round += 1;
    initTime();
  }

  function shuffle() {
    cards = cards.sort(() => 0.5 - Math.random())
  }

  const maxFlipCount = 2;
  let firstFlippedCardIndex = null;
  let flipCount = 0;
  let gameCleared = false;
  let gameOvered = false;

  function flipCard(index) {
    if (flipCount >= maxFlipCount) {
      return;
    }

    cards[index].flipped = true;
    flipCount += 1;

    if (flipCount === maxFlipCount) {
      if (checkMatch(firstFlippedCardIndex, index)) {
        handleCardMatch(index);
      } else {
        setTimeout(() => {
          resetFlip(index);
        }, 1000)
      }
    } else {
      firstFlippedCardIndex = index;
    }
  }

  function checkMatch(index1, index2) {
    const firstFlippedCard = cards[index1];
    const secondFlippedCard = cards[index2];

    if (firstFlippedCard.id === secondFlippedCard.id) {
      return true;
    } else {
      return false;
    }
  }

  function checkGameClear() {
    return cards.every(card => card.matched)
  }

  function handleCardMatch(index) {
    cards[firstFlippedCardIndex].matched = true;
    cards[index].matched = true;
    flipCount = 0;
    firstFlippedCardIndex = null;
    $score += 100;

    if (checkGameClear()) {
      gameCleared = true;
      updateScores();
      clearInterval(timer);
    }
  }

  function resetFlip(index) {
    cards[firstFlippedCardIndex].flipped = false;
    cards[index].flipped = false;
    flipCount = 0;
    firstFlippedCardIndex = null;
  }


  function updateScores() {
    let highScore = 0;
    highScore = localStorage.getItem("highScore") || 0;

    if($score > highScore) {
      localStorage.setItem("highScore", $score);
    }

    localStorage.setItem("lastScore", $score);

  }

  let timer

  function startTimer() {
    timer = setInterval(() => {
      $time -= 1;
      if ($time === 0) {
        gameOvered = true;
        clearInterval(timer);
        updateScores();
      }
    }, 1000);
  }

  startFirstGame();
</script>
<h1>game grid</h1>
<ul class="game-grid">
  {#each cards as card, index}
    <li class={card.flipped || card.matched ? "card":"card hidden"}>
      <button on:click={()=>flipCard(index)}>
        <img src="{card_data[card.id].imgUrl}" alt=""/>
      </button>
    </li>
  {/each}
</ul>
<button on:click={nextGame}>reset</button>
{#if gameCleared}
  <Modal title="Game Clear"
         scoreTitle="final score"
         score={$score}
         nextButtonText="next"
         onNextClick={nextGame}
         prevButtonText="home"
         onPrevClick={()=>page = "title"}
         ></Modal>
{/if}

{#if gameOvered}
  <Modal
      title="Game Over"
      scoreTitle="bonus score"
      score={$score}
      nextButtonText="retry"
      onNextClick={replayGame}
      prevButtonText="home"
      onPrevClick={()=>page = "title"}
      ></Modal>
{/if}

<style lang="scss">
  .game-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    grid-template-rows: repeat(4, 1fr);
    list-style-type: none;
    grid-gap: 2%;
    padding: 20px;

    button {
      width: 100%;
      height: 100%;
      padding: 0;
      background: transparent;
      border: none;
    }
  }

  .card {
    background: #ffffff;
    aspect-ratio: 1 / 1;
    padding: 20%;
    border-radius: 18%;

    img {
      width: 100%;
      object-fit: contain;
      display: block;
      aspect-ratio: 1 / 1;
    }
  }

  .game-grid .card.hidden {
    background: rgba(0, 0, 0, 0.5);

    img {
      opacity: 0;
    }

    padding: 0;
    background-image: url('images/quest.png');
    background-repeat: no-repeat;
    background-size: 30%;
    background-position: center;
  }
</style>