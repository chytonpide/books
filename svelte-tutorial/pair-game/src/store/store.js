import { writable } from 'svelte/store';


const defaultTime = 60
export const score = writable(0);
export const round = writable(1);
export const time = writable(defaultTime);


export const initRound = () => {
  round.set(1);
}

export const initScore = () => {
    score.set(0);
}

export const initTime = () => {
    time.set(defaultTime);
}




