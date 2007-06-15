// Copyright 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.tutorial.pages;

import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.Persist;

public class Guess
{
  @Persist
  private int _target;

  private int _guess;

  @Persist
  private String _message;

  @Persist
  private int _count;

  public String getMessage()
  {
    return _message;
  }

  @InjectPage
  private GameOver _gameOver;

  Object onActionFromLink(int guess)
  {
    _count++;

    if (guess == _target)
    {
      _gameOver.setup(_count);
      return _gameOver;
    }

    if (guess < _target)
      _message = String.format("%d is too low.", guess);
    else
      _message = String.format("%d is too high.", guess);

    return null;
  }

  public int getGuess()
  {
    return _guess;
  }

  public void setGuess(int guess)
  {
    _guess = guess;
  }

  void setup(int target)
  {
    _target = target;
    _count = 0;
  }

  public int getTarget()
  {
    return _target;
  }
}
